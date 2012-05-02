package analysis.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import classfile.code.MethodInvocation;

public class MethodCall {
	
	public final Value instance;
	public final List<Value> arguments;
	public final MethodInvocation invocation;
	
	public MethodCall(Value instance, List<Value> arguments, MethodInvocation invocation) {
		this.instance = instance;
		this.arguments = Collections.unmodifiableList(new ArrayList<>(arguments));
		this.invocation = invocation;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(instance == null ? "static " + invocation.method.enclosingClass : "(" + invocation.method.enclosingClass + ") " + instance);
		result.append(".").append(invocation.method.name).append("(");
		String sep = "";
		for (Value argument : arguments) {
			result.append(sep).append(argument);
			sep = ", ";
		}
		result.append(")");
		return result.toString();
	}

}
