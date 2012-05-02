package classfile;

public class MethodFlags {

	public final boolean isPublic, isPrivate, isProtected, isStatic, isFinal,
			isSynchronized, isBridge, isVarargs, isNative, isAbstract,
			isStrict, isSynthetic;

	public MethodFlags(int flags) {
		isPublic = (flags & ACC_PUBLIC) != 0;
		isPrivate = (flags & ACC_PRIVATE) != 0;
		isProtected = (flags & ACC_PROTECTED) != 0;
		isStatic = (flags & ACC_STATIC) != 0;
		isFinal = (flags & ACC_FINAL) != 0;
		isSynchronized = (flags & ACC_SYNCHRONIZED) != 0;
		isBridge = (flags & ACC_BRIDGE) != 0;
		isVarargs = (flags & ACC_VARARGS) != 0;
		isNative = (flags & ACC_NATIVE) != 0;
		isAbstract = (flags & ACC_ABSTRACT) != 0;
		isStrict = (flags & ACC_STRICT) != 0;
		isSynthetic = (flags & ACC_SYNTHETIC) != 0;
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		String sep = "";
		if (isPublic) {
			result.append(sep).append("public");
			sep = " ";
		}
		if (isPrivate) {
			result.append(sep).append("private");
			sep = " ";
		}
		if (isProtected) {
			result.append(sep).append("protected");
			sep = " ";
		}
		if (isStatic) {
			result.append(sep).append("static");
			sep = " ";
		}
		if (isFinal) {
			result.append(sep).append("final");
			sep = " ";
		}
		if (isSynchronized) {
			result.append(sep).append("synchronized");
			sep = " ";
		}
		if (isBridge) {
			result.append(sep).append("bridge");
			sep = " ";
		}
		if (isVarargs) {
			result.append(sep).append("varargs");
			sep = " ";
		}
		if (isNative) {
			result.append(sep).append("native");
			sep = " ";
		}
		if (isAbstract) {
			result.append(sep).append("abstract");
			sep = " ";
		}
		if (isStrict) {
			result.append(sep).append("strictfp");
			sep = " ";
		}
		if (isSynthetic) {
			result.append(sep).append("synthetic");
			sep = " ";
		}
		return result.toString();
	}

	private static final int ACC_PUBLIC = 0x0001, ACC_PRIVATE = 0x0002,
			ACC_PROTECTED = 0x0004, ACC_STATIC = 0x0008, ACC_FINAL = 0x0010,
			ACC_SYNCHRONIZED = 0x0020, ACC_BRIDGE = 0x0040,
			ACC_VARARGS = 0x0080, ACC_NATIVE = 0x0100, ACC_ABSTRACT = 0x0400,
			ACC_STRICT = 0x0800, ACC_SYNTHETIC = 0x1000;

}
