package classfile.code;

import classfile.ClassReference;

public class ArrayInstantiation {
	
	public static final int T_BOOLEAN = 4, T_CHAR = 5, T_FLOAT = 6,
			T_DOUBLE = 7, T_BYTE = 8, T_SHORT = 9, T_INT = 10, T_LONG = 11;
	
	public final ClassReference arrayClass;
	public final int dimensionsCreated;
	
	public ArrayInstantiation(ClassReference arrayClass, int dimensionsCreated) {
		this.arrayClass = arrayClass;
		this.dimensionsCreated = dimensionsCreated;
	}
	
	@Override
	public String toString() {
		return arrayClass + " (" + dimensionsCreated + " dimensions created)";
	}
	
}
