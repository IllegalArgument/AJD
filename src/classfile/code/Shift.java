package classfile.code;

public class Shift {
	
	public final ShiftType shiftType;
	public final ComputationalType operatingType;
	
	public Shift(ShiftType shiftType, ComputationalType operatingType) {
		this.shiftType = shiftType;
		this.operatingType = operatingType;
	}
	
	@Override
	public String toString() {
		return operatingType + " " + shiftType;
	}

}
