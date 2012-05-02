package analysis.data;

import java.util.LinkedList;
import java.util.List;

public class DataBlock {
	
	public final List<Assignment> assignments = new LinkedList<>();
	public final List<DataHandler> handlers = new LinkedList<>();
	public DataTransition transition;
	
	@Override
	public String toString() {
		return assignments + " " + transition;
	}

}
