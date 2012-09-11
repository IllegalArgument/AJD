package classfile.constant;

import classfile.ClassReference;
import classfile.Primitive;

/**
 * All of the {@link ConstantEntry} types that are used in this library. Each type also has an associated class type, which may be null. This class type is used to help when handling bytecode instructions that load items from the constant pool, e.g., ldc.
 * 
 * @author Aaron Willey
 * @version 0.1
 */
public enum ConstantType {
	
	//TODO: Consider handling the invokedynamic constant entries
	
	CLASS(ClassReference.fromName("java.lang.Class")),
	FIELD_REF(null), METHOD_REF(null), INTERFACE_METHOD_REF(null),
	STRING(ClassReference.fromName("java.lang.String")),
	INTEGER(ClassReference.fromPrimitive(Primitive.INT)), FLOAT(ClassReference.fromPrimitive(Primitive.FLOAT)),
	LONG(ClassReference.fromPrimitive(Primitive.LONG)), DOUBLE(ClassReference.fromPrimitive(Primitive.DOUBLE)),
	NAME_AND_TYPE(null), UTF8(null), NULL(ClassReference.NULL);
	
	/**
	 * The class representing the data contained by a <code>ConstantEntry</code> of this type, or null if no such class is applicable.
	 */
	public final ClassReference classType;
	
	/**
	 * Constructs a constant type with the given class of data
	 * 
	 * @param classType the class of data using this enum type
	 */
	private ConstantType(ClassReference classType) {
		this.classType = classType;
	}
	
}
