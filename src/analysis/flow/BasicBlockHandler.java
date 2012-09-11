package analysis.flow;

import classfile.ClassReference;

public class BasicBlockHandler {

	public final ClassReference exceptionType;
	public final int priority;
	
	public BasicBlockHandler(ClassReference exceptionType, int priority) {
		this.exceptionType = exceptionType;
		this.priority = priority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((exceptionType == null) ? 0 : exceptionType.hashCode());
		result = prime * result + priority;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BasicBlockHandler other = (BasicBlockHandler) obj;
		if (exceptionType == null) {
			if (other.exceptionType != null)
				return false;
		} else if (!exceptionType.equals(other.exceptionType))
			return false;
		if (priority != other.priority)
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return (exceptionType != null ? exceptionType : "finally") + " priority " + priority;
	}
	
}
