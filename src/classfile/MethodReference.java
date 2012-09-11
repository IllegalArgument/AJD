package classfile;

import java.nio.CharBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import classfile.constant.NameAndType;

public class MethodReference {
	
	public final ClassReference enclosingClass;
	public final String name;
	public final List<ClassReference> argTypes;
	public final ClassReference returnType;
	
	public MethodReference(ClassReference enclosingClass, NameAndType nat) {
		this(enclosingClass, nat.name, nat.type);
	}
	
	public MethodReference(ClassReference enclosingClass, String name, String type) {
		this.enclosingClass = enclosingClass;
		this.name = name;
		List<ClassReference> argTypes = new LinkedList<>();
		CharBuffer descriptor = CharBuffer.wrap(type);
		if (descriptor.get() != '(') {
			throw new ClassFormatException("Method descriptor must begin with '('!");
		}
		while (descriptor.get(descriptor.position()) != ')') {
			argTypes.add(new ClassReference(descriptor));
		}
		if (descriptor.get() != ')') {
			throw new ClassFormatException("Method descriptor must terminate argument list with ')'!");
		}
		this.argTypes = Collections.unmodifiableList(argTypes);
		this.returnType = new ClassReference(descriptor);
	}
	
	public boolean isInit() {
		return name.equals("<init>");
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(enclosingClass).append(".").append(name).append("(");
		String sep = "";
		for (ClassReference type : argTypes) {
			result.append(sep).append(type);
			sep = ", ";
		}
		result.append("):").append(returnType);
		return result.toString();
	}

}
