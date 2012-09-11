package analysis.flow;

import classfile.code.opcodes.CompareJump;
import classfile.code.opcodes.ConditionalJump;

public class BasicBlockConnection {
	
	public static final BasicBlockConnection
	FALLTHROUGH = new BasicBlockConnection(BasicBlockConnectionType.FALLTHROUGH, null),
	GOTO = new BasicBlockConnection(BasicBlockConnectionType.UNCONDITIONAL, null),
	SUBROUTINE = new BasicBlockConnection(BasicBlockConnectionType.SUBROUTINE, null),
	DEFAULT = new BasicBlockConnection(BasicBlockConnectionType.DEFAULT, null);
	
	public final BasicBlockConnectionType type;
	public final Object data;
	
	private BasicBlockConnection(BasicBlockConnectionType type, Object data) {
		this.type = type;
		this.data = data;
	}
	
	public static BasicBlockConnection fromConditionalJump(ConditionalJump jump) {
		return new BasicBlockConnection(BasicBlockConnectionType.IF, new Condition(ConditionType.CONDITIONAL, jump.conditionType, jump.condition));
	}
	
	public static BasicBlockConnection fromCompareJump(CompareJump jump) {
		return new BasicBlockConnection(BasicBlockConnectionType.IF, new Condition(ConditionType.COMPARE, jump.compareType, jump.comparison));
	}
	
	public static BasicBlockConnection fromSwitch(int switchCondition) {
		return new BasicBlockConnection(BasicBlockConnectionType.SWITCH, switchCondition);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
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
		BasicBlockConnection other = (BasicBlockConnection) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return type + (data != null ? " " + data : "");
	}

}
