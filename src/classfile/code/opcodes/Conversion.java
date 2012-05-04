package classfile.code.opcodes;

import classfile.Primitive;

public class Conversion {

	public final Primitive from;
	public final Primitive to;
	
	public Conversion(Primitive from, Primitive to) {
		this.from = from;
		this.to = to;
	}
	
	@Override
	public String toString() {
		return from + " to " + to;
	}
	
}
