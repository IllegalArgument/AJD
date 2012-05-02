package analysis.data;

public class ValueComparison {
	
	public final Value left, right;
	public final ComparisonType comparison;
	
	public ValueComparison(Value left, ComparisonType comparison, Value right) {
		this.left = left;
		this.comparison = comparison;
		this.right = right;
	}

	@Override
	public String toString() {
		return left + " " + comparison + " " + right;
	}

}
