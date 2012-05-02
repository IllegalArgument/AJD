package analysis.data;

public class Assignment {
	
	public final Value target, value;

	public Assignment(Value target, Value value) {
		this.target = target;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return target + " <- " + value;
	}

}
