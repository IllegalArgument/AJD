package analysis.data;

import classfile.ClassReference;
import classfile.ConstantEntry;
import classfile.ConstantType;
import classfile.Primitive;

public class Value {
	
	public static final Value
	POP = new Value(ValueType.POP, null, null),
	VOID = new Value(ValueType.VOID, null, null),
	MONITOR = new Value(ValueType.MONITOR, null, null),
	RELEASE = new Value(ValueType.RELEASE, null, null),
	NULL = new Value(ValueType.CONSTANT, ConstantEntry.NULL, ClassReference.NULL),
	ZERO = new Value(ValueType.CONSTANT, new ConstantEntry(ConstantType.INTEGER, 0), ClassReference.fromPrimitive(Primitive.INT));
	
	public final ValueType type;
	public final Object value;
	public final ClassReference classType;
	
	public Value(ValueType type, Object value, ClassReference classType) {
		this.type = type;
		this.value = value;
		this.classType = classType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result+ ((classType == null) ? 0 : classType.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Value other = (Value) obj;
		if (classType == null) {
			if (other.classType != null)
				return false;
		} else if (!classType.equals(other.classType))
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + type + (value != null ? " [" + value + "]" : "") + (classType != null ? ":" + classType : "") + ")";
	}

}
