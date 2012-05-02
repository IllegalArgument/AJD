package analysis.data;

import java.util.HashMap;
import java.util.Map;

public class DataTransition {
	
	public final DataTransitionType type;
	public final Object transition;
	public final Map<DataConnection, DataBlock> successors = new HashMap<>();
	
	private DataTransition(DataTransitionType type, Object transition) {
		this.type = type;
		this.transition = transition;
	}
	
	public static DataTransition fromFallThrough() {
		return new DataTransition(DataTransitionType.FALL_THROUGH, null);
	}
	
	public static DataTransition fromUnconditional() {
		return new DataTransition(DataTransitionType.UNCONDITIONAL, null);
	}
	
	public static DataTransition fromComparison(ValueComparison comparison) {
		return new DataTransition(DataTransitionType.COMPARISON, comparison);
	}
	
	public static DataTransition fromSwitch(Value value) {
		return new DataTransition(DataTransitionType.SWITCH, value);
	}
	
	public static DataTransition fromThrow() {
		return new DataTransition(DataTransitionType.THROW, null);
	}
	
	public static DataTransition fromReturn(Value returnValue) {
		return new DataTransition(DataTransitionType.RETURN, returnValue);
	}
	
	@Override
	public String toString() {
		return type + (transition != null ? " [" + transition + "]" : "");
	}

}
