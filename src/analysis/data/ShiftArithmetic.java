package analysis.data;

import classfile.code.ShiftType;

public class ShiftArithmetic {
	
	public final Value shifted, shiftAmount;
	public final ShiftType shiftType;
	
	public ShiftArithmetic(Value shifted, Value shiftAmount, ShiftType shiftType) {
		this.shifted = shifted;
		this.shiftAmount = shiftAmount;
		this.shiftType = shiftType;
	}
	
	@Override
	public String toString() {
		return shifted + " " + shiftType + " " + shiftAmount;
	}

}
