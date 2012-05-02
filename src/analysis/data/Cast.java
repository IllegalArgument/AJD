package analysis.data;

import classfile.ClassReference;

public class Cast {
	
	public final Value value;
	public final ClassReference type;
	
	public Cast(Value value, ClassReference type) {
		this.value = value;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return value + " to " + type;
	}

}
