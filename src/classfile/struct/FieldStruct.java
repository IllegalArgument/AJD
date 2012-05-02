package classfile.struct;

import java.nio.ByteBuffer;

import util.BufferUtils;

public class FieldStruct implements Struct<FieldStruct> {

	public int accessFlags;
	public int nameIndex;
	public int descriptorIndex;
	public int attributesCount;
	public AttributeStruct[] attributes;

	@Override
	public FieldStruct read(ByteBuffer buf) {
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
