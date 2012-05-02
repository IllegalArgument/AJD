package classfile;

public enum ConstantType {
	
	CLASS(ClassReference.fromName("java.lang.Class")),
	FIELD_REF(null), METHOD_REF(null), INTERFACE_METHOD_REF(null),
	STRING(ClassReference.fromName("java.lang.String")),
	INTEGER(ClassReference.fromPrimitive(Primitive.INT)), FLOAT(ClassReference.fromPrimitive(Primitive.FLOAT)),
	LONG(ClassReference.fromPrimitive(Primitive.LONG)), DOUBLE(ClassReference.fromPrimitive(Primitive.DOUBLE)),
	NAME_AND_TYPE(null), UTF8(null), NULL(ClassReference.NULL);
	
	public final ClassReference classType;
	
	private ConstantType(ClassReference classType) {
		this.classType = classType;
	}
	
}
