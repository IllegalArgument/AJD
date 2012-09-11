package analysis.flow;

import java.util.BitSet;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import classfile.code.opcodes.Opcode;

/**
 * <code>BasicBlocks</code> are a natural grouping of bytecode instructions during the control flow analysis phase. A basic block is defined as a section of code that has only one entry point and one exit point, though we relax this definition for exception handlers (since the JVM can potentially throw an exception at any time, each instruction would have its own basic block if we didn't treat the handlers this way). This class is mutable so that it can be reused if control flow is restructured during analysis.
 * 
 * @author Aaron Willey
 * @version 0.1
 */
public class BasicBlock {
	
	//TODO: Create actual accessors for these fields, although the creation of numerous getters and setters seems a bit pointless to me...
	
	public int startBci;
	public int endBci;
	
	public List<Opcode> body;
	
	public final EnumSet<BasicBlockType> flags = EnumSet.noneOf(BasicBlockType.class);

	public final Set<BasicBlock> predecessors = new HashSet<>();
	public final Map<BasicBlockConnection, BasicBlock> successors = new HashMap<>();
	public final Set<BasicBlock> handled = new HashSet<>();
	public final Map<BasicBlockHandler, BasicBlock> handlers = new HashMap<>();

	public BitSet localsIn;
	public BitSet localsOut;
	
	public Set<BasicBlock> dominators;
	public BasicBlock immediateDominator;
	
	/**
	 * Connects two basic blocks via a connection object.
	 * 
	 * @param predecessor the block from which the connection begins
	 * @param connection the data associated with the connection
	 * @param successor the target of the connection
	 */
	public static void connect(BasicBlock predecessor, BasicBlockConnection connection, BasicBlock successor) {
		predecessor.successors.put(connection, successor);
		successor.predecessors.add(predecessor);
	}
	
	/**
	 * Adds an exception handler to a block by creating a two-way connection between the handled block and the handler.
	 * 
	 * @param handled the block covered by the exception handler
	 * @param exception the data of the actual exception handler
	 * @param handler the block that handles the exception
	 */
	public static void connectHandler(BasicBlock handled, BasicBlockHandler exception, BasicBlock handler) {
		handled.handlers.put(exception, handler);
		handler.handled.add(handled);
	}
	
	/**
	 * Removes a block from a graph by disconnecting its references to other blocks and removing all references to the block.
	 * 
	 * @param block the block to remove from the graph
	 */
	public static void disconnect(BasicBlock block) {
		for (BasicBlock predecessor : block.predecessors) {
			predecessor.successors.remove(block);
		}
		for (BasicBlock successor : block.successors.values()) {
			successor.predecessors.remove(block);
		}
		block.predecessors.clear();
		block.successors.clear();
	}
	
	@Override
	public String toString() {
		return "BasicBlock@" + startBci + "-" + endBci + " " + flags + " dominated by " + (immediateDominator != null ? immediateDominator.startBci + "-" + immediateDominator.endBci : "none") + " | LiveLocals " + localsIn + " => " + localsOut;
	}

}
