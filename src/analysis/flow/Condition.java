package analysis.flow;

import classfile.code.opcodes.ComputationalType;

public class Condition {
	
	public final ConditionType type;
	public final ComputationalType operatingType;
	public final Object condition;
	
	public Condition(ConditionType type, ComputationalType operatingType, Object condition) {
		this.type = type;
		this.operatingType = operatingType;
		this.condition = condition;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((condition == null) ? 0 : condition.hashCode());
		result = prime * result
				+ ((operatingType == null) ? 0 : operatingType.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Condition other = (Condition) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (operatingType != other.operatingType)
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return type + " " + condition + " on " + operatingType;
	}

}
