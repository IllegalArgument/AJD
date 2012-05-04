package analysis.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import classfile.code.opcodes.Opcode;

public class BasicBlock {
	
	public final List<Opcode> body;
	public final Map<BasicBlock, Boolean> forwardEdges = new HashMap<>();
	public final Map<BasicBlock, Boolean> backwardEdges = new HashMap<>();
	
	public BasicBlock(List<Opcode> body) {
		this.body = Collections.unmodifiableList(new ArrayList<>(body));
	}
	
	public static void connect(BasicBlock predecessor, BasicBlock successor, boolean isBackEdge) {
		predecessor.forwardEdges.put(successor, isBackEdge);
		successor.backwardEdges.put(predecessor, isBackEdge);
	}
	
	@Override
	public String toString() {
		return body.toString();
	}

}
