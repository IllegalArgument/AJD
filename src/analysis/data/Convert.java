package analysis.data;

import classfile.Primitive;

public class Convert {
	
	public final Value value;
	public final Primitive type;
	
	public Convert(Value value, Primitive type) {
		this.value = value;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return value + " to " + type;
	}

}
