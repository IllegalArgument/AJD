package classfile.code;

public enum ComputationalType {
	
	INT(1), FLOAT(1), REFERENCE(1), RETURN_ADDRESS(1), LONG(2), DOUBLE(2), VOID(0);
	
	public final int category;
	
	private ComputationalType(int category) {
		this.category = category;
	}

}
