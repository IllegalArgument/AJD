package classfile.code.opcodes;

public class LocalVariable {
	
	public final int index;
	public final ComputationalType type;
	
	public LocalVariable(int index, ComputationalType type) {
		this.index = index;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return type + " at index " + index;
	}

}
