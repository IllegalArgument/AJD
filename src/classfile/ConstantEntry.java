package classfile;

public class ConstantEntry {
	
	public static ConstantEntry NULL = new ConstantEntry(ConstantType.NULL, null);
	
	public final ConstantType type;
	public final Object data;
	
	public ConstantEntry(ConstantType type, Object data) {
		this.type = type;
		this.data = data;
	}
	
	@Override
	public String toString() {
		return type + (data != null ? " [" + data + "]" : "");
	}

}
