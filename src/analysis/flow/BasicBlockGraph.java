package analysis.flow;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import util.PrettyPrinter;
import classfile.JavaMethod;
import classfile.code.Code;
import classfile.code.opcodes.CompareJump;
import classfile.code.opcodes.ConditionalJump;
import classfile.code.opcodes.ExceptionHandler;
import classfile.code.opcodes.LocalVariable;
import classfile.code.opcodes.LocalVariableIncrement;
import classfile.code.opcodes.Opcode;
import classfile.code.opcodes.Switch;

public class BasicBlockGraph {

	private final Code code;

	private int currentBci;
	private final Map<Integer, Set<Integer>> successors = new HashMap<>();
	private final NavigableMap<Integer, BasicBlock> blockMap = new TreeMap<>();
	private final List<BasicBlock> reversePostorder = new ArrayList<>();

	public BasicBlockGraph(JavaMethod method) {
		code = method.code;
		createBasicBlocks();
		computeBlockOrder();
		computeLiveLocals();
		computeDominators();
	}

	public String gml() {
		PrettyPrinter gml = new PrettyPrinter();
		gml.println("graph [")
		.indent()
		.println("directed 1")
		.println("node [")
		.indent()
		.println("id 0")
		.println("graphics [ fill \"#00FF00\" ]")
		.println("LabelGraphics [")
		.indent()
		.println("text \"Start\"")
		.println("fontName \"Monospaced\"")
		.println("alignment \"left\"")
		.println("fontStyle \"bold\"")
		.unindent()
		.println("]")
		.unindent()
		.println("]");
		for (BasicBlock block : reversePostorder) {
			gml.println("node [")
			.indent()
			.print("id ").println(Integer.toString(block.hashCode()))
			.print("LabelGraphics [")
			.indent()
			.println("fontName \"Monospaced\"")
			.println("alignment \"left\"")
			.println("fontStyle \"bold\"")
			.print("text \"")
			.println("Locals In: " + block.localsIn);
			for (Opcode op : block.body) {
				gml.println(op.toString().replace("\"", "").replace("&", ""));
			}
			gml.println("Locals Out: " + block.localsOut + "\"")
			.unindent()
			.println("]");
			int color = 0x000000;
			if (!block.handled.isEmpty()) {
				color |= 0xFF5555;
			}
			if (block.flags.contains(BasicBlockType.LOOP_HEADER)) {
				color |= 0x77FF77;
			}
			if (block.flags.contains(BasicBlockType.METHOD_END)) {
				color |= 0x9999FF;
			}
			if (color == 0x000000) {
				color = 0xFFBB00;
			}
			gml.println("graphics [ fill \"#" + Integer.toHexString(color) + "\" ]")
			.unindent()
			.println("]");
		}
		for (BasicBlock block : reversePostorder) {
			for (BasicBlock successor : block.successors) {
				gml.println("edge [")
				.indent()
				.print("source ").println(Integer.toString(block.hashCode()))
				.print("target ").println(Integer.toString(successor.hashCode()))
				.println("LabelGraphics [")
				.indent()
				.println("fontName \"Monospaced\"")
				.println("alignment \"left\"")
				.println("fontStyle \"bold\"")
				.println("text \"Successor\"")
				.println("outline \"#000000\"")
				.print("fill \"#FFBB00\"")
				.unindent()
				.println("]")
				.unindent()
				.println("]");
			}
			for (BasicBlock handler : block.handlers) {
				gml.println("edge [")
				.indent()
				.print("source ").println(Integer.toString(block.hashCode()))
				.print("target ").println(Integer.toString(handler.hashCode()))
				.println("LabelGraphics [")
				.indent()
				.println("fontName \"Monospaced\"")
				.println("alignment \"left\"")
				.println("fontStyle \"bold\"")
				.println("text \"Handler\"")
				.println("outline \"#000000\"")
				.print("fill \"#FF5555\"")
				.unindent()
				.println("]")
				.unindent()
				.println("]");
			}
		}
		gml.println("edge [")
		.indent()
		.println("source 0")
		.print("target ").println(Integer.toString(blockMap.get(0).hashCode()))
		.unindent()
		.println("]")
		.unindent()
		.println("]");
		return gml.toString();
	}

	private void createBasicBlocks() {
		List<Opcode> ops = code.ops;
		int codeSize = ops.size();
		NavigableSet<Integer> leaders = new TreeSet<>();
		leaders.add(0);
		blockMap.put(0, new BasicBlock());
		Set<Integer> rets = new HashSet<>();

		List<ExceptionHandler> exceptionTable = code.exceptionTable;
		for (ExceptionHandler handler : exceptionTable) {
			int startBci = handler.start;
			int endBci = handler.end;
			int handlerBci = handler.handler;
			leaders.add(startBci);
			leaders.add(endBci);
			leaders.add(handlerBci);
			makeBlock(startBci);
			makeBlock(endBci);
			BasicBlock handlerBlock = makeBlock(handlerBci);
			handlerBlock.flags.add(BasicBlockType.EXCEPTION_HANDLER);
		}

		for (currentBci = 0; currentBci < codeSize; currentBci++) {
			Opcode op = ops.get(currentBci);
			if (op == null) {
				continue;
			}
			int nextBci = -1;
			boolean hasNext = (currentBci < codeSize - 1);
			if (hasNext) {
				nextBci = code.next(currentBci);
			}
			switch (op.type) {
			case CONDITIONAL_JUMP:
			{
				int targetBci = ((ConditionalJump) op.data).jumpTarget;
				leaders.add(targetBci);
				makeBlock(targetBci);
				addSuccessor(currentBci, targetBci);
				addSuccessor(currentBci, nextBci);
				break;
			}
			case COMPARE_JUMP:
			{
				int targetBci = ((CompareJump) op.data).jumpTarget;
				leaders.add(targetBci);
				makeBlock(targetBci);
				addSuccessor(currentBci, targetBci);
				addSuccessor(currentBci, nextBci);
				break;
			}
			case UNCONDITIONAL_JUMP:
			{
				int targetBci = (int) op.data;
				leaders.add(targetBci);
				makeBlock(targetBci);
				addSuccessor(currentBci, targetBci);
				break;
			}
			case SUBROUTINE_JUMP:
			{
				int subroutineBci = (int) op.data;
				leaders.add(subroutineBci);
				BasicBlock subroutine = makeBlock(subroutineBci);
				subroutine.flags.add(BasicBlockType.SUBROUTINE_ENTRY);
				addSuccessor(currentBci, subroutineBci);
				break;
			}
			case RETURN:
			{
				break;
			}
			case SUBROUTINE_RETURN:
			{
				rets.add(currentBci);
				break;
			}
			case SWITCH:
			{
				Switch switchOp = (Switch) op.data;
				for (int switchTarget : switchOp.jumpTable.values()) {
					leaders.add(switchTarget);
					makeBlock(switchTarget);
					addSuccessor(currentBci, switchTarget);
				}
				int defaultTarget = switchOp.defaultJump;
				leaders.add(defaultTarget);
				makeBlock(defaultTarget);
				addSuccessor(currentBci, defaultTarget);
				break;
			}
			case THROW:
			{
				break;
			}
			default:
			{
				if (hasNext && leaders.contains(nextBci)) {
					addSuccessor(currentBci, nextBci);
				}
				continue;
			}
			}
			if (hasNext) {
				leaders.add(nextBci);
				makeBlock(nextBci);
			}
		}
		for (int ret : rets) {
			blockMap.floorEntry(ret).getValue().flags.add(BasicBlockType.SUBROUTINE_EXIT);
		}

		List<Object> nullList = Collections.singletonList(null);
		int finalLeader = leaders.last();
		for (int leader : leaders) {
			int nextLeader = (leader == finalLeader ? ops.size() : leaders.higher(leader));
			List<Opcode> body = new ArrayList<>(ops.subList(leader, nextLeader));
			body.removeAll(nullList);
			BasicBlock block = blockMap.get(leader);
			block.endBci = code.previous(nextLeader);
			block.body = body;
		}
		for (Map.Entry<Integer, Set<Integer>> successorEntry : successors.entrySet()) {
			int blockBci = successorEntry.getKey();
			BasicBlock predecessor = blockMap.get(leaders.floor(blockBci));
			Set<Integer> successors = successorEntry.getValue();
			for (int successor : successors) {
				BasicBlock.connect(predecessor, blockMap.get(successor));
			}
		}
		for (ExceptionHandler exceptionHandler : exceptionTable) {
			BasicBlock handler = blockMap.get(exceptionHandler.handler);
			for (BasicBlock handled : blockMap.subMap(exceptionHandler.start, exceptionHandler.end).values()) {
				BasicBlock.connectHandler(handled, handler);
			}
		}
	}

	private void computeBlockOrder() {
		computeBlockOrder(blockMap.get(0));
		Collections.reverse(reversePostorder);
	}

	private void computeBlockOrder(BasicBlock block) {
		EnumSet<BasicBlockType> flags = block.flags;
		if (flags.contains(BasicBlockType.VISITED)) {
			if (flags.contains(BasicBlockType.ACTIVE)) {
				flags.add(BasicBlockType.LOOP_HEADER);
			}
			return;
		}
		flags.add(BasicBlockType.VISITED);
		if (block.successors.isEmpty() && block.handlers.isEmpty()) {
			flags.add(BasicBlockType.METHOD_END);
		} else {
			flags.add(BasicBlockType.ACTIVE);
			for (BasicBlock successor : block.successors) {
				computeBlockOrder(successor);
			}
			for (BasicBlock handler : block.handlers) {
				computeBlockOrder(handler);
			}
			flags.remove(BasicBlockType.ACTIVE);
		}
		reversePostorder.add(block);
	}

	private void computeLiveLocals() {
		int localsCount = code.maxLocals;
		Map<BasicBlock, BitSet> localsLiveGen = new HashMap<>();
		Map<BasicBlock, BitSet> localsLiveKill = new HashMap<>();
		Map<BasicBlock, BitSet> localsLiveIn = new HashMap<>();
		Map<BasicBlock, BitSet> localsLiveOut = new HashMap<>();
		for (BasicBlock block : reversePostorder) {
			BitSet gen = new BitSet(localsCount);
			BitSet kill = new BitSet(localsCount);
			initializeBlockLiveness(block, gen, kill);
			localsLiveGen.put(block, gen);
			localsLiveKill.put(block, kill);
			localsLiveIn.put(block, new BitSet(localsCount));
			localsLiveOut.put(block, new BitSet(localsCount));
		}
		Queue<BasicBlock> queue = new LinkedList<>();
		for (int i = reversePostorder.size() - 1; i >= 0; i--) {
			BasicBlock block = reversePostorder.get(i);
			BitSet liveOut = localsLiveOut.get(block);
			for (BasicBlock successor : block.successors) {
				liveOut.or(localsLiveIn.get(successor));
			}
			for (BasicBlock handler : block.handlers) {
				liveOut.or(localsLiveIn.get(handler));
			}
			BitSet liveIn = liveOut.get(0, localsCount);
			liveIn.andNot(localsLiveKill.get(block));
			liveIn.or(localsLiveGen.get(block));
			localsLiveIn.put(block, liveIn);
			queue.add(block);
		}
		while (!queue.isEmpty()) {
			BasicBlock block = queue.poll();
			BitSet liveOut = localsLiveOut.get(block);
			BitSet originalLiveOut = liveOut.get(0, localsCount);
			for (BasicBlock successor : block.successors) {
				liveOut.or(localsLiveIn.get(successor));
			}
			for (BasicBlock handler : block.handlers) {
				liveOut.or(localsLiveIn.get(handler));
			}
			if (!originalLiveOut.equals(liveOut)) {
				BitSet liveIn = liveOut.get(0, localsCount);
				liveIn.andNot(localsLiveKill.get(block));
				liveIn.or(localsLiveGen.get(block));
				localsLiveIn.put(block, liveIn);
				queue.addAll(block.predecessors);
			}
		}
		for (Map.Entry<BasicBlock, BitSet> liveLocalsIn : localsLiveIn.entrySet()) {
			liveLocalsIn.getKey().localsIn = liveLocalsIn.getValue();
		}
		for (Map.Entry<BasicBlock, BitSet> liveLocalsOut : localsLiveOut.entrySet()) {
			liveLocalsOut.getKey().localsOut = liveLocalsOut.getValue();
		}
	}

	private void computeDominators() {
		for (BasicBlock block : reversePostorder) {
			block.dominators = new HashSet<>(reversePostorder);
		}
		Queue<BasicBlock> queue = new LinkedList<>();
		queue.add(reversePostorder.get(0));
		while (!queue.isEmpty()) {
			BasicBlock block = queue.poll();
			Set<BasicBlock> newDominators = new HashSet<>();
			if (!block.predecessors.isEmpty()) {
				newDominators.addAll(block.predecessors.iterator().next().dominators);
			} else if (!block.handled.isEmpty()) {
				newDominators.addAll(block.handled.iterator().next().dominators);
			}
			for (BasicBlock predecessor : block.predecessors) {
				newDominators.retainAll(predecessor.dominators);
			}
			for (BasicBlock handled : block.handled) {
				newDominators.retainAll(handled.dominators);
			}
			newDominators.add(block);
			if (!block.dominators.equals(newDominators)) {
				block.dominators = newDominators;
				for (BasicBlock successor : block.successors) {
					queue.add(successor);
				}
				for (BasicBlock handler : block.handlers) {
					queue.add(handler);
				}
			}
		}
		for (BasicBlock block : reversePostorder) {
			BasicBlock immediateDominator = null;
			for (BasicBlock dominator : block.dominators) {
				boolean isImmediateDominator = true;
				for (BasicBlock tempDominator : block.dominators) {
					if (tempDominator.dominators.contains(dominator)) {
						isImmediateDominator = false;
					}
				}
				if (isImmediateDominator) {
					immediateDominator = dominator;
				}
			}
			block.immediateDominator = immediateDominator;
		}
	}

	private static void initializeBlockLiveness(BasicBlock block, BitSet gen, BitSet kill) {
		for (Opcode op : block.body) {
			switch (op.type) {
			case LOCAL_LOAD:
			{
				LocalVariable local = (LocalVariable) op.data;
				int localIndex = local.index;
				if (!kill.get(localIndex)) {
					gen.set(localIndex);
				}
				if (local.type.category == 2) {
					if (!kill.get(localIndex + 1)) {
						gen.set(localIndex + 1);
					}
				}
				break;
			}
			case LOCAL_INCREMENT:
			{
				int localIndex = ((LocalVariableIncrement) op.data).local.index;
				if (!kill.get(localIndex)) {
					gen.set(localIndex);
				}
				break;
			}
			case SUBROUTINE_RETURN:
			{
				int localIndex = (int) op.data;
				if (!kill.get(localIndex)) {
					gen.set(localIndex);
				}
				break;
			}
			case LOCAL_STORE:
			{
				LocalVariable local = (LocalVariable) op.data;
				int localIndex = local.index;
				kill.set(localIndex);
				if (local.type.category == 2) {
					kill.set(localIndex + 1);
				}
				break;
			}
			}
		}
	}

	private void addSuccessor(int fromBci, int toBci) {
		if (successors.containsKey(fromBci)) {
			successors.get(fromBci).add(toBci);
		} else {
			Set<Integer> successor = new HashSet<>();
			successor.add(toBci);
			successors.put(fromBci, successor);
		}
	}

	private BasicBlock makeBlock(int bci) {
		if (!blockMap.containsKey(bci)) {
			BasicBlock newBlock = new BasicBlock();
			newBlock.startBci = bci;
			if (bci < currentBci) {
				int splitBci = blockMap.lowerKey(bci);
				if (successors.containsKey(splitBci)) {
					successors.get(splitBci).clear();
				}
				addSuccessor(splitBci, bci);
			}
			blockMap.put(bci, newBlock);
			return newBlock;
		} else {
			return blockMap.get(bci);
		}
	}

}
