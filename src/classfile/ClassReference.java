package classfile;

import java.nio.CharBuffer;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import classfile.code.opcodes.ComputationalType;

/**
 * A <code>ClassReference</code> is simply a reference to a class or primitive by name, with some additional features. Because the JVM internally treats types like <code>int[]</code> as a separate class, the array dimension is stored in these references as well. Note that a <code>ClassReference</code> does not contain any details on the contents of the class that it refers to; in order to obtain these, a {@link JavaClass} must be create from the actual class file, e.g., via a lookup from the {@link ClassStore}.
 * 
 * @author Aaron Willey
 * @version 0.1
 */
public class ClassReference {

	/**
	 * The set of all primitive types that the JVM internally treats as "int"s.
	 */
	public static final EnumSet<Primitive> INT_TYPES = EnumSet.of(Primitive.BOOLEAN, Primitive.BYTE, Primitive.CHAR, Primitive.SHORT);
	//create constant instances for some of the basic class types, like the primitives, Object, and null
	public static final ClassReference
	BOOLEAN = new ClassReference(Primitive.BOOLEAN, Primitive.BOOLEAN.name, 0),
	BYTE = new ClassReference(Primitive.BYTE, Primitive.BYTE.name, 0),	
	CHAR = new ClassReference(Primitive.CHAR, Primitive.CHAR.name, 0),
	SHORT = new ClassReference(Primitive.SHORT, Primitive.SHORT.name, 0),
	INT = new ClassReference(Primitive.INT, Primitive.INT.name, 0),
	LONG = new ClassReference(Primitive.LONG, Primitive.LONG.name, 0),
	FLOAT = new ClassReference(Primitive.FLOAT, Primitive.FLOAT.name, 0),
	DOUBLE = new ClassReference(Primitive.DOUBLE, Primitive.DOUBLE.name, 0),
	NULL = new ClassReference(Primitive.REFERENCE, "null", 0),
	OBJECT = new ClassReference(Primitive.REFERENCE, "java.lang.Object", 0);
	
	public final Primitive primitive;
	public final String className;
	public final int arrayDimension;

	/**
	 * Creates a new <code>ClassReference</code> from the given arguments (internal).
	 * 
	 * @param primitive the primitive type of this class
	 * @param className the name of this class
	 * @param arrayDimension the number of dimensions in this class if it an array class, otherwise 0
	 */
	private ClassReference(Primitive primitive, String className, int arrayDimension) {
		this.primitive = primitive;
		this.className = className;
		this.arrayDimension = arrayDimension;
	}

	/**
	 * Parses a class file descriptor to create an appropriate class reference.
	 * 
	 * @param descriptor the descriptor to parse
	 * @see ClassReference(CharBuffer) ClassReference
	 */
	public ClassReference(String descriptor) {
		this(CharBuffer.wrap(descriptor));
	}

	/**
	 * Parses a class file descriptor to create an appropriate class reference. The descriptor here must be properly formed; it cannot just be the class name (as is sometimes used in the constant pool).
	 * 
	 * @param descriptor the JVM internal descriptor for this class
	 */
	public ClassReference(CharBuffer descriptor) {
		Primitive primitive;
		String className;
		int arrayDimension = 0;
		char chr;
		//array dimensions are indicated by '['s at the beginning of the descriptor
		while ((chr = descriptor.get()) == '[') {
			arrayDimension++;
		}
		//then the class type of the descriptor is specified by the next character 
		switch (chr) {
		case 'B':
			primitive = Primitive.BYTE;
			className = primitive.name;
			break;
		case 'C':
			primitive = Primitive.CHAR;
			className = primitive.name;
			break;
		case 'D':
			primitive = Primitive.DOUBLE;
			className = primitive.name;
			break;
		case 'F':
			primitive = Primitive.FLOAT;
			className = primitive.name;
			break;
		case 'I':
			primitive = Primitive.INT;
			className = primitive.name;
			break;
		case 'J':
			primitive = Primitive.LONG;
			className = primitive.name;
			break;
		case 'L':
			//objects have their name following the 'L', terminated by a semicolon
			StringBuilder temp = new StringBuilder();
			while ((chr = descriptor.get()) != ';') {
				temp.append(chr);
			}
			//the JVM uses '/' instead of '.' for package/class separation
			className = temp.toString().replace('/', '.');
			primitive = Primitive.REFERENCE;
			break;
		case 'S':
			primitive = Primitive.SHORT;
			className = primitive.name;
			break;
		case 'V':
			primitive = Primitive.VOID;
			className = primitive.name;
			break;
		case 'Z':
			primitive = Primitive.BOOLEAN;
			className = primitive.name;
			break;
		default:
			throw new ClassFormatException("Invalid class type in descriptor!");
		}
		//this annoys me, but constructor calls can only be the first operation of a constructor, so we have to manually set things
		this.primitive = primitive;
		this.className = className;
		this.arrayDimension = arrayDimension;
	}

	/**
	 * Attempts to determine whether the class represented by this reference is a subclass of the class represented by another class reference. If the NULL class references are provided, or the full class files for any classes being examined during the search are not available, <code>false</code> is returned. Primitives are treated separately from references; however, all primitive types internally treated as "int" in the JVM are considered subclasses of "int". Also note that any class is a subclass of itself, and interfaces are treated as potential superclasses as well.
	 * 
	 * @param other the class to determine whether this is a subclass of it
	 * @return whether this represented class is a subclass of the argument
	 */
	public boolean isSubclassOf(ClassReference other) {
		//TODO: Review this code thoroughly, it hasn't been looked at in a while
		if (this.equals(other) || this.equals(NULL) || other.equals(NULL)) {
			return true;
		}
		boolean isPrimitive = (primitive != Primitive.REFERENCE && other.primitive != Primitive.REFERENCE);
		boolean isReference = (primitive == Primitive.REFERENCE && other.primitive == Primitive.REFERENCE);
		if (isPrimitive && isReference) {
			return false;
		} else if (isPrimitive) {
			if (other.primitive == Primitive.INT && INT_TYPES.contains(primitive)) {
				return true;
			} else {
				return false;
			}
		} else if (isReference) {
			Queue<ClassReference> tests = new LinkedList<>();
			tests.add(this);
			while (!tests.isEmpty()) {
				ClassReference test = tests.poll();
				if (!test.equals(OBJECT)) {
					JavaClass testClass = ClassStore.findClass(test);
					if (testClass != null) {
						ClassReference superType = testClass.superType;
						if (superType.equals(other)) {
							return true;
						}
						tests.add(superType);
						for (ClassReference interfaceType : testClass.interfaces) {
							if (interfaceType.equals(other)) {
								return true;
							}
							tests.add(interfaceType);
						}
					} else {
						return false;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Attempts to find the least common superclass from a set of classes. The semantics here are fairly similar to those in {@link ClassReference#isSubclassOf(ClassReference)}, in that primitives are separate from references, internal "int" types are subclasses of "int", <code>null</code> is returned if a class cannot be located, and interfaces are considered potential superclasses. 
	 * 
	 * @param classes the classes that are being examined to find their least common superclass
	 * @return the least common superclass of the given classes, or <code>null</code> if none was found
	 */
	public static ClassReference leastCommonSuperclass(Set<ClassReference> classes) {
		//TODO: Understand why this works, since I can't remember how I made it
		boolean isPrimitive = false;
		boolean isReference = false;
		for (ClassReference clazz : classes) {
			if (clazz.getComputationalType() != ComputationalType.REFERENCE) {
				isPrimitive = true;
			} else {
				isReference = true;
			}
		}
		if (isPrimitive && isReference) {
			return null;
		} else if (isPrimitive) {
			Set<Primitive> primitives = new HashSet<>();
			for (ClassReference clazz : classes) {
				primitives.add(clazz.primitive);
			}
			primitives.removeAll(INT_TYPES);
			if (primitives.isEmpty()) {
				return ClassReference.fromPrimitive(Primitive.INT);
			} else if (primitives.size() == 1) {
				return ClassReference.fromPrimitive(primitives.iterator().next());
			} else {
				return null;
			}
		} else if (isReference) {
			Set<ClassReference> found = new HashSet<>();
			Set<ClassReference> tests = new HashSet<>(classes);
			while (found.isEmpty() && !tests.isEmpty()) {
				Set<ClassReference> newTests = new HashSet<>();
				for (ClassReference test : tests) {
					boolean isSuper = true;
					for (ClassReference clazz : classes) {
						if (!clazz.isSubclassOf(test)) {
							isSuper = false;
						}
					}
					if (isSuper && !test.equals(NULL)) {
						found.add(test);
					}
					JavaClass testClass = ClassStore.findClass(test);
					if (testClass != null) {
						ClassReference superType = testClass.superType;
						if (superType != null) {
							newTests.add(superType);
						}
						newTests.addAll(testClass.interfaces);
					}
				}
				tests = newTests;
			}
			ClassReference result = NULL;
			for (ClassReference candidate : found) {
				if (candidate.isSubclassOf(result)) {
					result = candidate;
				}
			}
			return result;
		}
		return null;
	}

	/**
	 * Gets the computational type of the class represented by this reference.
	 * 
	 * @return the computational type of this class
	 */
	public ComputationalType getComputationalType() {
		if (arrayDimension == 0) {
			return primitive.computationalType;
		} else {
			return ComputationalType.REFERENCE;
		}
	}

	/**
	 * Creates a class reference from the string contents of a constant pool entry of type CONSTANT_CLASS. The only reason this is necessary is that a CONSTANT_CLASS entry represents all arrays (which are references) normally, but non-array standard classes just as their binary name. 
	 * 
	 * @param constant the constant pool class-representing string
	 * @return the converted class reference
	 */
	public static ClassReference fromConstant(String constant) {
		if (constant.startsWith("[")) {
			return new ClassReference(constant);
		} else {
			return fromName(constant);
		}
	}

	/**
	 * Returns the appropriate class reference for a given computational type.
	 * 
	 * @param type the computational type to examine
	 * @return the class of the given computational type
	 */
	public static ClassReference fromComputationalType(ComputationalType type) {
		switch (type) {
		case INT:
			return fromPrimitive(Primitive.INT);
		case FLOAT:
			return fromPrimitive(Primitive.FLOAT);
		case REFERENCE:
			return fromPrimitive(Primitive.REFERENCE);
		case RETURN_ADDRESS:
			throw new RuntimeException("Return address is evil!");
		case LONG:
			return fromPrimitive(Primitive.LONG);
		case DOUBLE:
			return fromPrimitive(Primitive.DOUBLE);
		case VOID:
			return fromPrimitive(Primitive.VOID);
		default:
			return null;
		}
	}

	/**
	 * Returns the appropriate class reference for a given primitive type.
	 * 
	 * @param primitive the primitive to examine
	 * @return the class of the given primitive
	 */
	public static ClassReference fromPrimitive(Primitive primitive) {
		switch (primitive) {
		case BOOLEAN:
			return BOOLEAN;
		case BYTE:
			return BYTE;
		case CHAR:
			return CHAR;
		case SHORT:
			return SHORT;
		case INT:
			return INT;
		case LONG:
			return LONG;
		case FLOAT:
			return FLOAT;
		case DOUBLE:
			return DOUBLE;
		case REFERENCE:
			return OBJECT;
		default:
			return null;
		}
	}

	/**
	 * Creates a class reference corresponding to an array of Java primitives, e.g., <code>int[][]</code>.
	 * 
	 * @param primitive the primitive type of the array
	 * @param dimension the number of dimensions in the array
	 * @return the corresponding class reference
	 */
	public static ClassReference arrayFromPrimitive(Primitive primitive, int dimension) {
		if (primitive == Primitive.REFERENCE) {
			return arrayFromClass(OBJECT, dimension);
		} else {
			return new ClassReference(primitive, primitive.name, dimension);
		}
	}

	/**
	 * Returns a class reference for a class with a given name that is not an array.
	 * 
	 * @param name the name of the class
	 * @return the corresponding class reference to the given name
	 */
	public static ClassReference fromName(String name) {
		return new ClassReference(Primitive.REFERENCE, name.replace('/', '.'), 0);
	}

	/**
	 * Returns a class reference for the class of an array of items with a given name, with the specified number of dimensions.
	 * 
	 * @param name the name of the class of the items in the array
	 * @param dimension the number of dimensions in the array
	 * @return the corresponding class reference to the array type
	 */
	public static ClassReference arrayFromName(String name, int dimension) {
		return new ClassReference(Primitive.REFERENCE, name.replace('/', '.'), dimension);
	}
	
	/**
	 * Returns a class reference of an array of items of the type of a given class reference, with the specified number of dimensions.
	 * 
	 * @param element the class type of the elements of the array
	 * @param dimension the number of dimensions in the array
	 * @return the corresponding class reference to the array type
	 */
	public static ClassReference arrayFromClass(ClassReference element, int dimension) {
		return new ClassReference(element.primitive, element.className, dimension);
	}

	/**
	 * Returns the class reference of what an array of the given element would be, i.e., adding one more dimension to the given class reference.
	 * 
	 * @param elementType the class type of the elements in the array
	 * @return the array containing items of the given class type
	 */
	public static ClassReference arrayFromElementType(ClassReference elementType) {
		return new ClassReference(elementType.primitive, elementType.className, elementType.arrayDimension + 1);
	}

	/**
	 * Returns the class reference of the type of the elements in a given array class. Note that, for example, the returned type for a class <code>int[][]</code> would be <code>int[]</code>, not <code>int</code>.
	 * 
	 * @param arrayType the array class to determine what elements it contains
	 * @return the class reference to the individual elements in the array
	 */
	public static ClassReference elementFromArrayType(ClassReference arrayType) {
		return new ClassReference(arrayType.primitive, arrayType.className, arrayType.arrayDimension - 1);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + arrayDimension;
		result = prime * result
				+ ((className == null) ? 0 : className.hashCode());
		result = prime * result
				+ ((primitive == null) ? 0 : primitive.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassReference other = (ClassReference) obj;
		if (arrayDimension != other.arrayDimension)
			return false;
		if (className == null) {
			if (other.className != null)
				return false;
		} else if (!className.equals(other.className))
			return false;
		if (primitive != other.primitive)
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(className);
		for (int i = 0; i < arrayDimension; i++) {
			result.append("[]");
		}
		return result.toString();
	}

}
