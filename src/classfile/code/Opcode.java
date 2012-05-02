package classfile.code;

public class Opcode {

	public final OpType type;
	public final Object data;
	
	public Opcode(OpType type, Object data) {
		this.type = type;
		this.data = data;
	}
	
	@Override
	public String toString() {
		return type + (data != null ? " [" + data + "]" : "");
	}

}
