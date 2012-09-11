package classfile;

import java.nio.ByteBuffer;

import util.BufferUtils;
import util.PrettyPrinter;
import util.Printable;
import classfile.constant.ConstantEntry;
import classfile.struct.AttributeStruct;
import classfile.struct.FieldStruct;

public class JavaField implements Printable {
	
	public final FieldFlags flags;
	public final FieldReference reference;
	public final boolean isSynthetic;
	public final boolean isDeprecated;
	public final ConstantEntry constantValue;

	public JavaField(JavaClass enclosingClass, FieldStruct struct) {
		flags = new FieldFlags(struct.accessFlags);
		reference = new FieldReference(enclosingClass.thisType,
				(String) enclosingClass.getConstant(struct.nameIndex).data,
				(String) enclosingClass.getConstant(struct.descriptorIndex).data);
		boolean isSynthetic = false;
		boolean isDeprecated = false;
		ConstantEntry constantValue = null;
		for (int i = 0; i < struct.attributesCount; i++) {
			String attributeName = (String) enclosingClass.getConstant(struct.attributes[i].attributeNameIndex).data;
			ByteBuffer attributeInfo = struct.attributes[i].info;
			switch (attributeName) {
			case AttributeStruct.SYNTHETIC:
				isSynthetic = true;
				break;
			case AttributeStruct.DEPRECATED:
				isDeprecated = true;
				break;
			case AttributeStruct.CONSTANT_VALUE:
				constantValue = enclosingClass.getConstant(BufferUtils.getUnsignedShort(attributeInfo));
				break;
			}
		}
		this.isSynthetic = isSynthetic;
		this.isDeprecated = isDeprecated;
		this.constantValue = constantValue;
	}

	@Override
	public void printOn(PrettyPrinter p) {
		p.println("Field " + reference + " [")
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
		if (constantValue != null) {
			p.println("Constant value: " + constantValue);
		}
		p.unindent()
		.println("]");
	}

}
