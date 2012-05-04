package classfile.code.stackmap;

public class VerificationItem {

	public static final int TOP = 0, INTEGER = 1, FLOAT = 2, LONG = 3, DOUBLE = 4, NULL = 5, UNINITIALIZED_THIS = 6, OBJECT = 7, UNINITIALIZED = 8;

	public static final VerificationItem
	TOP_ITEM = new VerificationItem(VerificationType.TOP, null),
	INTEGER_ITEM = new VerificationItem(VerificationType.INTEGER, null),
	FLOAT_ITEM = new VerificationItem(VerificationType.FLOAT, null),
	LONG_ITEM = new VerificationItem(VerificationType.LONG, null),
	DOUBLE_ITEM = new VerificationItem(VerificationType.DOUBLE, null),
	NULL_ITEM = new VerificationItem(VerificationType.NULL, null),
	UNINITIALIZED_THIS_ITEM = new VerificationItem(VerificationType.UNINITIALIZED_THIS, null);

	public final VerificationType type;
	public final Object data;

	public VerificationItem(VerificationType type, Object data) {
		this.type = type;
		this.data = data;
	}
	
	@Override
	public String toString() {
		return type + (data != null ? " " + data : "");
	}

}
