package analysis.flow;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import classfile.code.opcodes.Opcode;

public class BasicBlock {
	
	public int startBci;
	public int endBci;
	
	public List<Opcode> body;
	
	public final EnumSet<BasicBlockType> flags = EnumSet.noneOf(BasicBlockType.class);

	public final Set<BasicBlock> predecessors = new HashSet<>();
	public final Set<BasicBlock> successors = new HashSet<>();
	public final Set<BasicBlock> handled = new HashSet<>();
	public final Set<BasicBlock> handlers = new HashSet<>();

	public BitSet localsIn;
	public BitSet localsOut;
	
	public Set<BasicBlock> dominators;
	public BasicBlock immediateDominator;
	
	public static void connect(BasicBlock predecessor, BasicBlock successor) {
		predecessor.successors.add(successor);
		successor.predecessors.add(predecessor);
	}
	
	public static void connectHandler(BasicBlock handled, BasicBlock handler) {
		handled.handlers.add(handler);
		handler.handled.add(handled);
	}
	
	public static void disconnect(BasicBlock block) {
		for (BasicBlock predecessor : block.predecessors) {
			predecessor.successors.remove(block);
		}
		for (BasicBlock successor : block.successors) {
			successor.predecessors.remove(block);
		}
		block.predecessors.clear();
		block.successors.clear();
	}
	
	@Override
	public String toString() {
		return "BasicBlock@" + startBci + "-" + endBci + " " + flags + " LiveLocals " + localsIn + " => " + localsOut;
	}

}
