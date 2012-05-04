package classfile.code.opcodes;

import classfile.ClassReference;

public class ExceptionHandler {
	
	public final int start, end;
	public final int handler;
	public final ClassReference catchType;
	
	public ExceptionHandler(int start, int end, int handler, ClassReference catchType) {
		this.start = start;
		this.end = end;
		this.handler = handler;
		if (catchType == null) {
			this.catchType = new ClassReference("Ljava/lang/Throwable;");
		} else {
			this.catchType = catchType;
		}
	}
	
	@Override
	public String toString() {
		return "[" + start + ", " + end + "): " + catchType + " handled at " + handler;
	}

}
