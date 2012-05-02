package classfile;

public class ClassFlags {

	public final boolean isPublic, isFinal, isSuper, isInterface, isAbstract,
			isSynthetic, isAnnotation, isEnum;

	public ClassFlags(int flags) {
		isPublic = (flags & ACC_PUBLIC) != 0;
		isFinal = (flags & ACC_FINAL) != 0;
		isSuper = (flags & ACC_SUPER) != 0;
		isInterface = (flags & ACC_INTERFACE) != 0;
		isAbstract = (flags & ACC_ABSTRACT) != 0;
		isSynthetic = (flags & ACC_SYNTHETIC) != 0;
		isAnnotation = (flags & ACC_ANNOTATION) != 0;
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
		if (isFinal) {
			result.append(sep).append("final");
			sep = " ";
		}
		if (isSuper) {
			result.append(sep).append("super");
			sep = " ";
		}
		if (isInterface) {
			result.append(sep).append("interface");
			sep = " ";
		}
		if (isAbstract) {
			result.append(sep).append("abstract");
			sep = " ";
		}
		if (isSynthetic) {
			result.append(sep).append("synthetic");
			sep = " ";
		}
		if (isAnnotation) {
			result.append(sep).append("annotation");
			sep = " ";
		}
		if (isEnum) {
			result.append(sep).append("enum");
			sep = " ";
		}
		return result.toString();
	}

	private static final int ACC_PUBLIC = 0x0001, ACC_FINAL = 0x0010,
			ACC_SUPER = 0x0020, ACC_INTERFACE = 0x0200, ACC_ABSTRACT = 0x0400,
			ACC_SYNTHETIC = 0x1000, ACC_ANNOTATION = 0x2000, ACC_ENUM = 0x4000;

}
