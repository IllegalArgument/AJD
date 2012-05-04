package classfile.code.opcodes;

import classfile.FieldReference;

public class FieldAccessor {
	
	public final FieldReference field;
	public final boolean isStatic;
	
	public FieldAccessor(FieldReference field, boolean isStatic) {
		this.field = field;
		this.isStatic = isStatic;
	}
	
	@Override
	public String toString() {
		return (isStatic ? "static " : "") + field;
	}

}
