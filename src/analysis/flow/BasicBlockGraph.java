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

/**
 * A graph of the basic blocks from the <code>Code</code> attribute of a method. A basic block is a segment of code such that control flow cannot enter it except from one entry point, and leaves through one exit point. An exception to this rule are exception handlers (forgive the pun), which only divide basic blocks at the start and end of their handling range, and the start of the actual handler code. An instance of this class constructs a <code>Map</code> of bytecode indices to their respective {@link BasicBlock}s, and connects the <code>BasicBlocks</code> to their successors.
 * <p>
 * In addition to computing the positions of the basic blocks and forming the connections between them, a <code>BasicBlockGraph</code> performs two additional analyses at the moment - live local variable analysis and dominator computation. Live local analysis computes which local variables are needed to enter and exit each basic block such that the program can execute properly. This can be used to eliminate useless variables, such as those introduced by obfuscation techniques. Dominator computation finds dominating blocks for each basic block. A dominator of a basic block is a block through which control flow must always pass before reaching the dominated block.
 * 
 * @author Aaron Willey
 * @version 0.1
 */
public class BasicBlockGraph {
	
	//TODO: Add accessor methods to the graph first, and then work on transformation methods
	//TODO: Think about the way in which exception handlers are treated and how they affect the division of and connections between basic blocks

	//the code that this graph works on
	private final Code code;

	//a temporary variable for storing the current bytecode index, used to construct the initial basic block structure
	private int currentBci;
	//another temporary variable, this one recording the links between basic blocks before the basic block objects are fully created
	private final Map<Integer, Map<BasicBlockConnection, Integer>> successors = new HashMap<>();
	//the map of bytecode indices to basic blocks
	private final NavigableMap<Integer, BasicBlock> blockMap = new TreeMap<>();
	//the basic blocks stored in reverse postorder (which is not the same as preorder)
	private final List<BasicBlock> reversePostorder = new ArrayList<>();

	/**
	 * Constructs a graph of basic blocks from a bytecode method. This includes reverse postorder calculation, live variable analysis, and dominator computation for the code.
	 * 
	 * @param method the method to construct the graph from
	 */
	public BasicBlockGraph(JavaMethod method) {
		code = method.code;
		if (code == null) {
			return;
		}
		createBasicBlocks();
		computeBlockOrder();
		computeLiveLocals();
		computeDominators();
		for (BasicBlock block : reversePostorder) {
			System.out.println(block + " " + block.body);
		}
	}

	/**
	 * Creates the GML markup of a graph that represents this code. This is mainly used for diagnostic purposes, and is subject to change at any time.
	 * 
	 * @return a string representation of the the GML markup to produce the graph
	 */
	public String gml() {
		PrettyPrinter gml = new PrettyPrinter();
		gml.println("graph [")
		.indent()
		.println("directed 1")
		.println("node [")
		.indent()
		.println("id -1")
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
			.print("id ").println(Integer.toString(block.startBci))
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
			for (Map.Entry<BasicBlockConnection, BasicBlock> successor : block.successors.entrySet()) {
				gml.println("edge [")
				.indent()
				.print("source ").println(Integer.toString(block.startBci))
				.print("target ").println(Integer.toString(successor.getValue().startBci))
				.println("LabelGraphics [")
				.indent()
				.println("fontName \"Monospaced\"")
				.println("alignment \"left\"")
				.println("fontStyle \"bold\"")
				.print("text \"").print(successor.getKey().toString()).println("\"")
				.println("outline \"#000000\"")
				.print("fill \"#FFBB00\"")
				.unindent()
				.println("]")
				.println("graphics [ arrow \"last\" ]")
				.unindent()
				.println("]");
			}
			for (Map.Entry<BasicBlockHandler, BasicBlock> handler : block.handlers.entrySet()) {
				gml.println("edge [")
				.indent()
				.print("source ").println(Integer.toString(block.startBci))
				.print("target ").println(Integer.toString(handler.getValue().startBci))
				.println("LabelGraphics [")
				.indent()
				.println("fontName \"Monospaced\"")
				.println("alignment \"left\"")
				.println("fontStyle \"bold\"")
				.print("text \"").print(handler.getKey().toString()).println("\"")
				.println("outline \"#000000\"")
				.print("fill \"#FF5555\"")
				.unindent()
				.println("]")
				.println("graphics [ arrow \"last\" ]")
				.unindent()
				.println("]");
			}
		}
		gml.println("edge [")
		.indent()
		.println("source -1")
		.print("target ").println(Integer.toString(blockMap.get(0).startBci))
		.unindent()
		.println("]")
		.unindent()
		.println("]");
		return gml.toString();
	}


	/**
	 * Creates the map of bytecode indices to their respective basic blocks. This is done by first identifying the boundaries of basic blocks and saving the connections until all blocks have been created, at which point the blocks are then linked.
	 */
	private void createBasicBlocks() {
		//DISCLAIMER: I can't recall if the order of some of these computations matters, but I'm pretty sure the current order is correct

		List<Opcode> ops = code.ops;
		//leaders are the beginning bcis of a basic block, sorted for convenience
		NavigableSet<Integer> leaders = new TreeSet<>();
		//manually add the block for bci 0, since it always exists
		leaders.add(0);
		blockMap.put(0, new BasicBlock());
		//TODO: Think about how to handle JSR and RET.
		Set<Integer> rets = new HashSet<>();

		List<ExceptionHandler> exceptionTable = code.exceptionTable;
		//the start, end, and handler bcis of an exception handler all start a new basic block
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

		//these two variables have to be outside the for loop so that they persist between iterations
		int nextBci;
		//must be initialized to true so that the first loop will start
		boolean hasNext = true;
		//current bci has to be a field so that makeBlock() can use it to determine if a block needs to be split
		for (currentBci = 0; hasNext; currentBci = nextBci) {
			Opcode op = ops.get(currentBci);
			nextBci = code.next(currentBci);
			//Code.next() returns a negative value if no such opcode exists, so that's how we determine if there's a next opcode (somewhat hack-ish, but I guess that's what lack of multiple returns does)
			hasNext = (nextBci > 0);
			switch (op.type) {
			case CONDITIONAL_JUMP:
			{
				//conditional jumps have two successors - the jump and the fallthrough, each being a leader
				ConditionalJump jump = (ConditionalJump) op.data;
				int targetBci = jump.jumpTarget;
				leaders.add(targetBci);
				makeBlock(targetBci);
				addSuccessor(currentBci, BasicBlockConnection.fromConditionalJump(jump), targetBci);
				addSuccessor(currentBci, BasicBlockConnection.FALLTHROUGH, nextBci);
				break;
			}
			case COMPARE_JUMP:
			{
				//comparison jumps are basically the same as conditional jumps in terms of control flow
				CompareJump jump = (CompareJump) op.data;
				int targetBci = jump.jumpTarget;
				leaders.add(targetBci);
				makeBlock(targetBci);
				addSuccessor(currentBci, BasicBlockConnection.fromCompareJump(jump), targetBci);
				addSuccessor(currentBci, BasicBlockConnection.FALLTHROUGH, nextBci);
				break;
			}
			case UNCONDITIONAL_JUMP:
			{
				//unconditional jumps only have one successor, but the fallthrough is still a leader
				int targetBci = (int) op.data;
				leaders.add(targetBci);
				makeBlock(targetBci);
				addSuccessor(currentBci, BasicBlockConnection.GOTO, targetBci);
				break;
			}
			case SUBROUTINE_JUMP:
			{
				//subroutines are just weird, but they can only jump to one place, so they have one successor, which is now a leader
				int subroutineBci = (int) op.data;
				leaders.add(subroutineBci);
				BasicBlock subroutine = makeBlock(subroutineBci);
				subroutine.flags.add(BasicBlockType.SUBROUTINE_ENTRY);
				addSuccessor(currentBci, BasicBlockConnection.SUBROUTINE, subroutineBci);
				break;
			}
			case RETURN:
			{
				//returns break up control flow, but aren't succeeded by anything
				break;
			}
			case SUBROUTINE_RETURN:
			{
				//I have utterly no idea how to handle RET, so just keep track of them for now
				rets.add(currentBci);
				break;
			}
			case SWITCH:
			{
				//switches have successors to each case and the default, making each of these a leader
				Switch switchOp = (Switch) op.data;
				for (Map.Entry<Integer, Integer> switchCondition : switchOp.jumpTable.entrySet()) {
					int switchTarget = switchCondition.getValue();
					leaders.add(switchTarget);
					makeBlock(switchTarget);
					addSuccessor(currentBci, BasicBlockConnection.fromSwitch(switchCondition.getKey()), switchTarget);
				}
				int defaultTarget = switchOp.defaultJump;
				leaders.add(defaultTarget);
				makeBlock(defaultTarget);
				addSuccessor(currentBci, BasicBlockConnection.DEFAULT, defaultTarget);
				break;
			}
			case THROW:
			{
				//like return, throw just breaks up control flow
				break;
			}
			default:
			{
				//in the case of none of these special instructions, we check to see if the next instruction is a leader, and if so, we connect this block to the next one via a fallthrough
				if (hasNext && leaders.contains(nextBci)) {
					addSuccessor(currentBci, BasicBlockConnection.FALLTHROUGH, nextBci);
				}
				//we don't want to always create new blocks though, so we continue the loop
				continue;
			}
			}
			//if any of the special instructions in the switch above are found, the following instruction must start a new basic block, so the break from those cases brings control here
			if (hasNext) {
				leaders.add(nextBci);
				makeBlock(nextBci);
			}
		}
		//label things as subroutine exits for fun
		for (int ret : rets) {
			blockMap.floorEntry(ret).getValue().flags.add(BasicBlockType.SUBROUTINE_EXIT);
		}

		//this is just so that I can use List.removeAll() to get rid of all of the nulls in the opcode listings
		List<Object> nullList = Collections.singletonList(null);
		//the last (highest bci) leader, so that we can easily check if there's no following leader
		int finalLeader = leaders.last();
		for (int leader : leaders) {
			//if this is the last leader, then the range of opcodes to grab will be from the current leader to the end of the opcode list
			int nextLeader = (leader == finalLeader ? ops.size() : leaders.higher(leader));
			List<Opcode> body = new ArrayList<>(ops.subList(leader, nextLeader));
			body.removeAll(nullList);
			BasicBlock block = blockMap.get(leader);
			//note that the end bci of a block is the bci of the start of its last instruction, not the end of the last instruction
			block.endBci = code.previous(nextLeader);
			block.body = body;
		}
		//successors basically maps a bci contained in a basic block to a Map of connections to bcis of the successor basic blocks
		//this loops iterates over these entries and connects the actual basic block objects
		//the reason for it being done this way is that we know all of the basic blocks exist and are defined correctly now
		for (Map.Entry<Integer, Map<BasicBlockConnection, Integer>> successorEntry : successors.entrySet()) {
			int blockBci = successorEntry.getKey();
			//because of the keys of the successors Map being anywhere in a basic block, we have to find the closest leader less than or equal to the stored bci to find the actual start bci of the block
			BasicBlock predecessor = blockMap.get(leaders.floor(blockBci));
			Map<BasicBlockConnection, Integer> successors = successorEntry.getValue();
			for (Map.Entry<BasicBlockConnection, Integer> successor : successors.entrySet()) {
				BasicBlock.connect(predecessor, successor.getKey(), blockMap.get(successor.getValue()));
			}
		}
		int exceptionCount = exceptionTable.size();
		//exception handlers are easier to handle, since we don't have to track their ranges and successors
		for (int priority = 0; priority < exceptionCount; priority++) {
			ExceptionHandler exceptionHandler = exceptionTable.get(priority);
			BasicBlock handler = blockMap.get(exceptionHandler.handler);
			BasicBlockHandler exception = new BasicBlockHandler(exceptionHandler.catchType, priority);
			for (BasicBlock handled : blockMap.subMap(exceptionHandler.start, exceptionHandler.end).values()) {
				BasicBlock.connectHandler(handled, exception, handler);
			}
		}
	}


	/**
	 * Computes the reverse-postorder order of the basic blocks. Note that exception handlers are treated as successors to the block(s) they handle.
	 */
	private void computeBlockOrder() {
		//start with the entry block of the code, calling the recursive method that adds the blocks in postorder
		computeBlockOrder(blockMap.get(0));
		//reverse the list to get, surprise, reverse postorder
		Collections.reverse(reversePostorder);
	}

	/**
	 * Recursively adds all of the successor blocks to <code>block</code>, and then adds <code>block</code> itself, to the reverse-postorder list.
	 * 
	 * @param block the block currently being inspected
	 */
	private void computeBlockOrder(BasicBlock block) {
		EnumSet<BasicBlockType> flags = block.flags;
		//the block has already been visited, we don't process it again
		if (flags.contains(BasicBlockType.VISITED)) {
			//but if the block is also active, we've found a loop, and label it as such
			if (flags.contains(BasicBlockType.ACTIVE)) {
				flags.add(BasicBlockType.LOOP_HEADER);
			}
			return;
		}
		//first mark the block as visited, so that if we encounter it during recursive steps, we know not to process it
		flags.add(BasicBlockType.VISITED);
		//a block with no successors or handlers must be an end of a method
		if (block.successors.isEmpty() && block.handlers.isEmpty()) {
			flags.add(BasicBlockType.METHOD_END);
		} else {
			//since we're now working on the block, we flag it as active
			flags.add(BasicBlockType.ACTIVE);
			//then we recurse over the successors...
			for (BasicBlock successor : block.successors.values()) {
				computeBlockOrder(successor);
			}
			//...and the handlers
			for (BasicBlock handler : block.handlers.values()) {
				computeBlockOrder(handler);
			}
			//once we're done, the active flag can be removed
			flags.remove(BasicBlockType.ACTIVE);
		}
		//finally, having processed everything "after" this block, we can add it to the list
		reversePostorder.add(block);
	}

	/**
	 * Computes the local variables used in a basic block using live variable analysis. In simple terms, a variable is live at the start of a block if it is needed in that block or any of its successors; it is live at the end of a block if it is just needed by the successors.
	 */
	private void computeLiveLocals() {
		int localsCount = code.maxLocals;
		//the gen and kill sets are stored in temporary maps, since we only care about live-in and live-out right now
		Map<BasicBlock, BitSet> localsLiveGen = new HashMap<>();
		Map<BasicBlock, BitSet> localsLiveKill = new HashMap<>();
		//populate the gen and kill sets, and initialize the live-in and live-out sets
		for (BasicBlock block : reversePostorder) {
			BitSet gen = new BitSet(localsCount);
			BitSet kill = new BitSet(localsCount);
			initializeBlockLiveness(block, gen, kill);
			localsLiveGen.put(block, gen);
			localsLiveKill.put(block, kill);
			//we can initialize all variables to dead optimistically, and fix things later on
			block.localsIn = new BitSet(localsCount);
			block.localsOut = new BitSet(localsCount);
		}
		//our working queue of basic blocks
		Queue<BasicBlock> queue = new LinkedList<>();
		//first populate the queue with the postorder (backwards reverse postorder) sorted blocks, flagging them all as first iteration live analysis blocks
		for (int i = reversePostorder.size() - 1; i >= 0; i--) {
			BasicBlock block = reversePostorder.get(i);
			block.flags.add(BasicBlockType.LIVE_FIRST_ITERATION);
			queue.add(block);
		}
		//keep working as long as there's something to process
		while (!queue.isEmpty()) {
			BasicBlock block = queue.poll();
			BitSet liveOut = block.localsOut;
			//create a copy of the live-out set at the start so that we can compare later
			BitSet originalLiveOut = liveOut.get(0, localsCount);
			//live-out is the union of all of the live-in sets of the successors of the block
			for (BasicBlock successor : block.successors.values()) {
				liveOut.or(successor.localsIn);
			}
			//handlers count as successors too
			for (BasicBlock handler : block.handlers.values()) {
				liveOut.or(handler.localsIn);
			}
			//for the first iteration, we always have to update the live-in set, and if anything has changed in the live-out set, we have to do the same
			if (block.flags.remove(BasicBlockType.LIVE_FIRST_ITERATION) || !originalLiveOut.equals(liveOut)) {
				BitSet liveIn = liveOut.get(0, localsCount);
				//live-in = gen union (live-out minus kill)
				liveIn.andNot(localsLiveKill.get(block));
				liveIn.or(localsLiveGen.get(block));
				block.localsIn = liveIn;
				//since there was a change, we have to continue working backwards to propagate it
				queue.addAll(block.predecessors);
			}
		}
	}

	/**
	 * Computes the dominators and immediate dominator of each basic block. A block dominates another block n if going through it is the only way to reach n. The immediate dominator of a block n is the block that dominates n but does not dominate any other block that dominates n and is not equal to n (in simpler terms, it is the closest nonequal dominator of n).
	 */
	private void computeDominators() {
		//initialize every block to be dominated by every block, since we are looking for a maximal solution to the data flow equations
		for (BasicBlock block : reversePostorder) {
			block.dominators = new HashSet<>(reversePostorder);
		}
		Queue<BasicBlock> queue = new LinkedList<>();
		//we begin our queue with the start block of the code
		queue.add(reversePostorder.get(0));
		while (!queue.isEmpty()) {
			BasicBlock block = queue.poll();
			Set<BasicBlock> newDominators = new HashSet<>();
			//choose one predecessor type block of the block being examined, and add its dominators to the new dominators set
			//this is a kinda awkward step, but it's necessary because there's no way (that I know of) to compute the intersection of a number of sets, so we just start with one predecessor set as a base point, and then start intersecting everything
			if (!block.predecessors.isEmpty()) {
				newDominators.addAll(block.predecessors.iterator().next().dominators);
			} else if (!block.handled.isEmpty()) {
				newDominators.addAll(block.handled.iterator().next().dominators);
			}
			//now we can compute the intersection, specifically, the intersection of all of the dominators of each predecessor of the block
			for (BasicBlock predecessor : block.predecessors) {
				newDominators.retainAll(predecessor.dominators);
			}
			for (BasicBlock handled : block.handled) {
				newDominators.retainAll(handled.dominators);
			}
			//plus the block itself, since every block dominates itself
			newDominators.add(block);
			//check whether the dominator set of the block has changed, and if it has, set the dominators of the block to the new set, and add every successor to the work queue
			if (!block.dominators.equals(newDominators)) {
				block.dominators = newDominators;
				for (BasicBlock successor : block.successors.values()) {
					queue.add(successor);
				}
				for (BasicBlock handler : block.handlers.values()) {
					queue.add(handler);
				}
			}
		}
		//to compute the immediate dominator of each block, we look at the dominators of every block, and check whether the candidate dominator dominates any of the other dominators of the block
		//if this is so, then it can't be an immediate dominator, so only the block that satisfies the condition is saved as the immediate dominator
		for (BasicBlock block : reversePostorder) {
			for (BasicBlock candidateDominator : block.dominators) {
				//a block dominates itself, but is not an immediate dominator of itself
				if (candidateDominator == block) {
					continue;
				}
				//XXX: This control structure just bothers me, but I don't know how to fix it
				boolean isImmediateDominator = true;
				for (BasicBlock tempDominator : block.dominators) {
					//if any of the other strictly dominating blocks of the block we're working on are strictly dominated by the candidate dominator, the candidate cannot be an immediate dominator, so we break
					if (tempDominator != block && tempDominator != candidateDominator && tempDominator.dominators.contains(candidateDominator)) {
						isImmediateDominator = false;
						break;
					}
				}
				//if we found an immediate dominator, it has to be the only one for the block (I think)
				if (isImmediateDominator) {
					block.immediateDominator = candidateDominator;
					break;
				}
			}
		}
	}

	/**
	 * Initializes the gen and kill set arguments according to the given basic block. The gen set is the set of local variables used in the block before they are assigned to any value, and the kill set is the set of locals assigned to a value in the block.
	 * 
	 * @param block the block to process
	 * @param gen the gen set to initialize
	 * @param kill the kill set to initialize
	 */
	private static void initializeBlockLiveness(BasicBlock block, BitSet gen, BitSet kill) {
		for (Opcode op : block.body) {
			switch (op.type) {
			case LOCAL_LOAD:
			{
				//local load instructions are assumed to be use of the variable, so if the variable hasn't already been killed in this block, it gets added to gen
				LocalVariable local = (LocalVariable) op.data;
				int localIndex = local.index;
				if (!kill.get(localIndex)) {
					gen.set(localIndex);
					//type 2 locals also take up the slot above them, so it also gets added to gen
					if (local.type.category == 2) {
						gen.set(localIndex + 1);
					}
				}
				break;
			}
			case LOCAL_INCREMENT:
			{
				//local increment gens and then kills a variable, since it both uses and assigns to it 
				int localIndex = ((LocalVariableIncrement) op.data).local.index;
				if (!kill.get(localIndex)) {
					gen.set(localIndex);
				}
				kill.set(localIndex);
				break;
			}
			case SUBROUTINE_RETURN:
			{
				//subroutines are evil, but they also use a local variable to store the return address, so it is a gen candidate
				int localIndex = (int) op.data;
				if (!kill.get(localIndex)) {
					gen.set(localIndex);
				}
				break;
			}
			case LOCAL_STORE:
			{
				//storing a local is assigning it, so it goes to the kill set
				LocalVariable local = (LocalVariable) op.data;
				int localIndex = local.index;
				kill.set(localIndex);
				if (local.type.category == 2) {
					//and if it's type 2, the local above it is also killed
					kill.set(localIndex + 1);
				}
				break;
			}
			default:
			{
				break;
			}
			}
		}
	}

	/**
	 * Stores a connection between two blocks in a temporary map so that they can be later formally linked.
	 * 
	 * @param fromBci the bytecode index of an instruction contained by the block where the connection starts
	 * @param connection the actual connection object linking the two blocks
	 * @param toBci the bytecode index of the target block of this connection
	 */
	private void addSuccessor(int fromBci, BasicBlockConnection connection, int toBci) {
		if (successors.containsKey(fromBci)) {
			//add the connection
			successors.get(fromBci).put(connection, toBci);
		} else {
			//create a new map and add the connection, then store the map
			Map<BasicBlockConnection, Integer> successor = new HashMap<>();
			successor.put(connection, toBci);
			successors.put(fromBci, successor);
		}
	}

	/**
	 * Returns the basic block object for a specific bytecode index, creating one if necessary. If the new basic block is to be created from a backwards jump, the basic block before the new block is "split" and then connected to the created block.
	 * 
	 * @param bci the bytecode index of the block to make, if needed
	 * @return the block that was created or found
	 */
	private BasicBlock makeBlock(int bci) {
		if (!blockMap.containsKey(bci)) {
			//we need to make a new block since it doesn't already exist
			BasicBlock newBlock = new BasicBlock();
			newBlock.startBci = bci;
			//if the bci of the block we're making is lower than our current position in the code, i.e., a backwards jump occurred, we need to connect the two pieces of the split block together
			if (bci < currentBci) {
				//find the closest block below the bci where we want the new block
				int splitBci = blockMap.lowerKey(bci);
				//TODO: Figure out why I put this here - there's probably a reason, but I can't remember it, and it might have just been legacy if there was a reason. It doesn't seem to make a difference in test cases though.
				/*if (successors.containsKey(splitBci)) {
					successors.get(splitBci).clear();
				}*/
				//and connect that old block to the new one with a fallthrough
				addSuccessor(splitBci, BasicBlockConnection.FALLTHROUGH, bci);
			}
			blockMap.put(bci, newBlock);
			return newBlock;
		} else {
			//there's already a block, so just return it
			return blockMap.get(bci);
		}
	}

}
