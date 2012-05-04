package classfile.code.opcodes;

public class CompareJump {
	
	public final CompareCondition comparison;
	public final ComputationalType compareType;
	public final int jumpTarget;
	
	public CompareJump(CompareCondition comparison, ComputationalType compareType, int jumpTarget) {
		this.comparison = comparison;
		this.compareType = compareType;
		this.jumpTarget = jumpTarget;
	}
	
	@Override
	public String toString() {
		return comparison + " on " + compareType + " to " + jumpTarget;
	}

}
