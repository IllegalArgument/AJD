package classfile.struct;

import java.nio.ByteBuffer;

import util.BufferUtils;

public class ClassStruct implements Struct<ClassStruct> {

	public int magic;
	public int minorVersion, majorVersion;
	public int constantPoolCount;
	public ConstantEntryStruct[] constantPool;
	public int accessFlags;
	public int thisClass;
	public int superClass;
	public int interfacesCount;
	public int[] interfaces;
	public int fieldsCount;
	public FieldStruct[] fields;
	public int methodsCount;
	public MethodStruct[] methods;
	public int attributesCount;
	public AttributeStruct[] attributes;

	@Override
	public ClassStruct read(ByteBuffer buf) {
		magic = buf.getInt();
		minorVersion = BufferUtils.getUnsignedShort(buf);
		majorVersion = BufferUtils.getUnsignedShort(buf);
		constantPoolCount = BufferUtils.getUnsignedShort(buf);
		constantPool = new ConstantEntryStruct[constantPoolCount];
		for (int i = 1; i < constantPoolCount; i++) {
			ConstantEntryStruct cp = new ConstantEntryStruct().read(buf);
			constantPool[i] = cp;
			if (cp.isDoubleSize()) {
				i++;
			}
		}
		accessFlags = BufferUtils.getUnsignedShort(buf);
		thisClass = BufferUtils.getUnsignedShort(buf);
		superClass = BufferUtils.getUnsignedShort(buf);
		interfacesCount = BufferUtils.getUnsignedShort(buf);
		interfaces = new int[interfacesCount];
		for (int i = 0; i < interfacesCount; i++) {
			interfaces[i] = BufferUtils.getUnsignedShort(buf);
		}
		fieldsCount = BufferUtils.getUnsignedShort(buf);
		fields = new FieldStruct[fieldsCount];
		for (int i = 0; i < fieldsCount; i++) {
			fields[i] = new FieldStruct().read(buf);
		}
		methodsCount = BufferUtils.getUnsignedShort(buf);
		methods = new MethodStruct[methodsCount];
		for (int i = 0; i < methodsCount; i++) {
			methods[i] = new MethodStruct().read(buf);
		}
		attributesCount = BufferUtils.getUnsignedShort(buf);
		attributes = new AttributeStruct[attributesCount];
		for (int i = 0; i < attributesCount; i++) {
			attributes[i] = new AttributeStruct().read(buf);
		}
		return this;
	}

}
