package classfile.code.opcodes;

public class ConditionalJump {
	
	public final JumpCondition condition;
	public final ComputationalType conditionType;
	public final int jumpTarget;
	
	public ConditionalJump(JumpCondition condition, ComputationalType conditionType, int jumpTarget) {
		this.condition = condition;
		this.conditionType = conditionType;
		this.jumpTarget = jumpTarget;
	}
	
	@Override
	public String toString() {
		return condition + " on " + conditionType + " to " + jumpTarget;
	}

}
