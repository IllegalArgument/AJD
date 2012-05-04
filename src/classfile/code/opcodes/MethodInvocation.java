package classfile.code.opcodes;

import classfile.MethodReference;

public class MethodInvocation {
	
	public final MethodReference method;
	public final MethodType type;
	
	public MethodInvocation(MethodReference method, MethodType type) {
		this.method = method;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return method + " (" + type + ")";
	}
	
}
