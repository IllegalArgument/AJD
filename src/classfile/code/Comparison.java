package classfile.code;

public class Comparison {
	
	public final ComputationalType compareType;
	public final CompareOption option;
	
	
	public Comparison(ComputationalType compareType, CompareOption option) {
		this.compareType = compareType;
		this.option = option;
	}
	
	@Override
	public String toString() {
		return option + " on " + compareType;
	}

}
