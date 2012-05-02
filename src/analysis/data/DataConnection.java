package analysis.data;

public class DataConnection {
	
	public static final DataConnection
	UNCONDITIONAL = new DataConnection(DataConnectionType.UNCONDITIONAL, null),
	IF = new DataConnection(DataConnectionType.IF, null),
	ELSE = new DataConnection(DataConnectionType.ELSE, null),
	DEFAULT = new DataConnection(DataConnectionType.DEFAULT, null),
	FALL_THROUGH = new DataConnection(DataConnectionType.FALL_THROUGH, null);
	
	public final DataConnectionType type;
	public final Object condition;
	
	private DataConnection(DataConnectionType type, Object condition) {
		this.type = type;
		this.condition = condition;
	}
	
	public static DataConnection fromSwitch(int switchValue) {
		return new DataConnection(DataConnectionType.SWITCH, switchValue);
	}
	
	@Override
	public String toString() {
		return type + (condition != null ? " [" + condition + "]" : "");
	}

}
