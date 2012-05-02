package classfile.code;

public class LocalVariableIncrement {
	
	public final LocalVariable local;
	public final int incrementAmount;
	
	public LocalVariableIncrement(LocalVariable local, int incrementAmount) {
		this.local = local;
		this.incrementAmount = incrementAmount;
	}
	
	@Override
	public String toString() {
		return local + " by " + incrementAmount;
	}

}
