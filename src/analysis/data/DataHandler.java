package analysis.data;

import classfile.ClassReference;

public class DataHandler {
	
	public final ClassReference exception;
	public final DataBlock handler;
	
	public DataHandler(ClassReference exception, DataBlock handler) {
		this.exception = exception;
		this.handler = handler;
	}

	@Override
	public String toString() {
		return exception.toString();
	}

}
