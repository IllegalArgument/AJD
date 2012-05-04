package classfile;

import java.nio.CharBuffer;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import classfile.code.opcodes.ComputationalType;

public class ClassReference {

	public static final EnumSet<Primitive> INT_TYPES = EnumSet.of(Primitive.BOOLEAN, Primitive.BYTE, Primitive.CHAR, Primitive.SHORT);
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

	private ClassReference(Primitive primitive, String className, int arrayDimension) {
		this.primitive = primitive;
		this.className = className;
		this.arrayDimension = arrayDimension;
	}

	public ClassReference(String descriptor) {
		this(CharBuffer.wrap(descriptor));
	}

	public ClassReference(CharBuffer descriptor) {
		Primitive primitive;
		String className;
		int arrayDimension = 0;
		char chr;
		while ((chr = descriptor.get()) == '[') {
			arrayDimension++;
		}
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
			StringBuilder temp = new StringBuilder();
			while ((chr = descriptor.get()) != ';') {
				temp.append(chr);
			}
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
		this.primitive = primitive;
		this.className = className;
		this.arrayDimension = arrayDimension;
	}

	public boolean isSubclassOf(ClassReference other) {
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

	public static ClassReference leastCommonSuperclass(Set<ClassReference> classes) {
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

	public ComputationalType getComputationalType() {
		if (arrayDimension == 0) {
			return primitive.computationalType;
		} else {
			return ComputationalType.REFERENCE;
		}
	}

	public static ClassReference fromConstant(String constant) {
		if (constant.startsWith("[")) {
			return new ClassReference(constant);
		} else {
			return fromName(constant);
		}
	}

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

	public static ClassReference arrayFromPrimitive(Primitive primitive, int dimension) {
		if (primitive == Primitive.REFERENCE) {
			return arrayFromClass(OBJECT, dimension);
		} else {
			return new ClassReference(primitive, primitive.name, dimension);
		}
	}

	public static ClassReference fromName(String name) {
		return new ClassReference(Primitive.REFERENCE, name.replace('/', '.'), 0);
	}

	public static ClassReference arrayFromName(String name, int dimension) {
		return new ClassReference(Primitive.REFERENCE, name.replace('/', '.'), dimension);
	}
	
	public static ClassReference arrayFromClass(ClassReference element, int dimension) {
		return new ClassReference(element.primitive, element.className, dimension);
	}

	public static ClassReference arrayFromElementType(ClassReference elementType) {
		return new ClassReference(elementType.primitive, elementType.className, elementType.arrayDimension + 1);
	}

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
