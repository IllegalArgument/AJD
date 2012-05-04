package analysis.data;

import classfile.code.opcodes.CompareOption;

public class Compare {
	
	public final Value value1, value2;
	public final CompareOption option;

	public Compare(Value value1, Value value2, CompareOption option) {
		this.value1 = value1;
		this.value2 = value2;
		this.option = option;
	}
	
	@Override
	public String toString() {
		return value1 + " compared to " + value2 + " (" + option + ")";
	}

}
