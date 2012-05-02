package classfile.code;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Switch {
	
	public final Map<Integer, Integer> jumpTable;
	public final int defaultJump;
	
	public Switch(Map<Integer, Integer> jumpTable, int defaultJump) {
		this.jumpTable = Collections.unmodifiableMap(new HashMap<>(jumpTable));
		this.defaultJump = defaultJump;
	}
	
	@Override
	public String toString() {
		return jumpTable + ", default " + defaultJump;
	}

}
