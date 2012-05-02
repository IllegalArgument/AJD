package classfile;

public class FieldFlags {

	public final boolean isPublic, isPrivate, isProtected, isStatic, isFinal,
			isVolatile, isTransient, isSynthetic, isEnum;

	public FieldFlags(int flags) {
		isPublic = (flags & ACC_PUBLIC) != 0;
		isPrivate = (flags & ACC_PRIVATE) != 0;
		isProtected = (flags & ACC_PROTECTED) != 0;
		isStatic = (flags & ACC_STATIC) != 0;
		isFinal = (flags & ACC_FINAL) != 0;
		isVolatile = (flags & ACC_VOLATILE) != 0;
		isTransient = (flags & ACC_TRANSIENT) != 0;
		isSynthetic = (flags & ACC_SYNTHETIC) != 0;
		isEnum = (flags & ACC_ENUM) != 0;
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
		if (isVolatile) {
			result.append(sep).append("volatile");
			sep = " ";
		}
		if (isTransient) {
			result.append(sep).append("transient");
			sep = " ";
		}
		if (isSynthetic) {
			result.append(sep).append("synthetic");
			sep = " ";
		}
		if (isEnum) {
			result.append(sep).append("enum");
			sep = " ";
		}
		return result.toString();
	}

	private static final int ACC_PUBLIC = 0x0001, ACC_PRIVATE = 0x0002,
			ACC_PROTECTED = 0x0004, ACC_STATIC = 0x0008, ACC_FINAL = 0x0010,
			ACC_VOLATILE = 0x0040, ACC_TRANSIENT = 0x0080,
			ACC_SYNTHETIC = 0x1000, ACC_ENUM = 0x4000;

}
