package analysis.data;

import classfile.code.opcodes.ArithmeticType;

public class BinaryArithmetic {
	
	public final Value left, right;
	public final ArithmeticType arithmetic;
	
	public BinaryArithmetic(Value left, Value right, ArithmeticType arithmetic) {
		this.left = left;
		this.right = right;
		this.arithmetic = arithmetic;
	}
	
	@Override
	public String toString() {
		return left + " " + arithmetic + " " + right;
	}

}
