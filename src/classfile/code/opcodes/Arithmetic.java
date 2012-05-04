package classfile.code.opcodes;


public class Arithmetic {
	
	public final ArithmeticType operation;
	public final ComputationalType operatingType;
	
	public Arithmetic(ArithmeticType operation, ComputationalType operatingType) {
		this.operation = operation;
		this.operatingType = operatingType;
	}
	
	@Override
	public String toString() {
		return operation + " " + operatingType;
	}

}
