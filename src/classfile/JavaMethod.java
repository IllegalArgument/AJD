package classfile;

import util.PrettyPrinter;
import util.Printable;
import classfile.code.Code;
import classfile.struct.AttributeStruct;
import classfile.struct.MethodStruct;

public class JavaMethod implements Printable {
	
	public final MethodFlags flags;
	public final MethodReference reference;
	public final boolean isSynthetic;
	public final boolean isDeprecated;
	public final Code code;
	
	public JavaMethod(JavaClass enclosingClass, MethodStruct struct) {
		flags = new MethodFlags(struct.accessFlags);
		reference = new MethodReference(enclosingClass.thisType,
				(String) enclosingClass.getConstant(struct.nameIndex).data,
				(String) enclosingClass.getConstant(struct.descriptorIndex).data);
		boolean isSynthetic = false;
		boolean isDeprecated = false;
		Code code = null;
		for (int i = 0; i < struct.attributesCount; i++) {
			AttributeStruct attribute = struct.attributes[i];
			String attributeName = (String) enclosingClass.getConstant(attribute.attributeNameIndex).data;
			if (attributeName.equals(AttributeStruct.SYNTHETIC)) {
				isSynthetic = true;
			}
			if (attributeName.equals(AttributeStruct.DEPRECATED)) {
				isDeprecated = true;
			}
			if (attributeName.equals(AttributeStruct.CODE)) {
				code = new Code(enclosingClass, attribute);
			}
		}
		this.isSynthetic = isSynthetic;
		this.isDeprecated = isDeprecated;
		this.code = code;
	}

	@Override
	public void printOn(PrettyPrinter p) {
		p.println("Method " + reference + " [")
		.indent()
		.println("Flags: " + flags);
		if (isSynthetic || isDeprecated) {
			p.print("Attributes: ");
			if (isSynthetic && isDeprecated) {
				p.println("synthetic deprecated");
			} else if (isSynthetic) {
				p.println("synthetic");
			} else {
				p.println("deprecated");
			}
		}
		if (code != null) {
			p.print(code);
		}
		p.unindent()
		.println("]");
	}

}
