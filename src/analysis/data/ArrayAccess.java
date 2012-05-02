package analysis.data;

public class ArrayAccess {
	
	public final Value reference;
	public final Value index;
	
	public ArrayAccess(Value reference, Value index) {
		this.reference = reference;
		this.index = index;
	}
	
	@Override
	public String toString() {
		return reference + "[" + index + "]";
	}

}
