package analysis.data;

import classfile.ClassReference;

public class InstanceOf {
	
	public final Value value;
	public final ClassReference test;
	
	public InstanceOf(Value value, ClassReference test) {
		this.value = value;
		this.test = test;
	}
	
	@Override
	public String toString() {
		return value + " instanceof " + test;
	}

}
