package analysis.data;

import classfile.code.CompareCondition;
import classfile.code.JumpCondition;

public enum ComparisonType {
	
	EQUAL, NOT_EQUAL, LESS, GREATER_EQUAL, GREATER, LESS_EQUAL;
	
	public static ComparisonType fromCondition(JumpCondition condition) {
		switch (condition) {
		case EQUAL_ZERO:
			return EQUAL;
		case NOT_EQUAL_ZERO:
			return NOT_EQUAL;
		case LESS_ZERO:
			return LESS;
		case GREATER_EQUAL_ZERO:
			return GREATER_EQUAL;
		case GREATER_ZERO:
			return GREATER;
		case LESS_EQUAL_ZERO:
			return LESS_EQUAL;
		case IS_NULL:
			return EQUAL;
		case IS_NOT_NULL:
			return NOT_EQUAL;
		default:
			return null;
		}
	}
	
	public static ComparisonType fromCompare(CompareCondition compare) {
		switch (compare) {
		case COMPARE_EQUAL:
			return EQUAL;
		case COMPARE_NOT_EQUAL:
			return NOT_EQUAL;
		case COMPARE_LESS:
			return LESS;
		case COMPARE_GREATER_EQUAL:
			return GREATER_EQUAL;
		case COMPARE_GREATER:
			return GREATER;
		case COMPARE_LESS_EQUAL:
			return LESS_EQUAL;
		default:
			return null;
		}
	}

}
