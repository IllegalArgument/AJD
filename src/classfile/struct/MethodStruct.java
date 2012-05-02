package classfile.struct;

import java.nio.ByteBuffer;

import util.BufferUtils;

public class MethodStruct implements Struct<MethodStruct> {

	public int accessFlags;
	public int nameIndex;
	public int descriptorIndex;
	public int attributesCount;
	public AttributeStruct[] attributes;

	@Override
	public MethodStruct read(ByteBuffer buf) {
		accessFlags = BufferUtils.getUnsignedShort(buf);
		nameIndex = BufferUtils.getUnsignedShort(buf);
		descriptorIndex = BufferUtils.getUnsignedShort(buf);
		attributesCount = BufferUtils.getUnsignedShort(buf);
		attributes = new AttributeStruct[attributesCount];
		for (int i = 0; i < attributesCount; i++) {
			attributes[i] = new AttributeStruct().read(buf);
		}
		return this;
	}

}

