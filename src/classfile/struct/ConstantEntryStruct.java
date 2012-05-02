package classfile.struct;

import java.nio.ByteBuffer;

import util.BufferUtils;

public class ConstantEntryStruct implements Struct<ConstantEntryStruct> {

	public static final int CLASS = 7, FIELD_REF = 9, METHOD_REF = 10,
			INTERFACE_METHOD_REF = 11, STRING = 8, INTEGER = 3, FLOAT = 4,
			LONG = 5, DOUBLE = 6, NAME_AND_TYPE = 12, UTF8 = 1,
			METHOD_HANDLE = 15, METHOD_TYPE = 16, INVOKE_DYNAMIC = 18;

	public int tag;
	public byte[] info;

	boolean isDoubleSize() {
		return tag == LONG || tag == DOUBLE;
	}

	@Override
	public ConstantEntryStruct read(ByteBuffer buf) {
		tag = BufferUtils.getUnsignedByte(buf);
		switch (tag) {
		case CLASS:
		case STRING:
		case METHOD_TYPE:
			info = new byte[2];
			break;
		case METHOD_HANDLE:
			info = new byte[3];
			break;
		case FIELD_REF:
		case METHOD_REF:
		case INTERFACE_METHOD_REF:
		case NAME_AND_TYPE:
		case INTEGER:
		case FLOAT:
		case INVOKE_DYNAMIC:
			info = new byte[4];
			break;
		case LONG:
		case DOUBLE:
			info = new byte[8];
			break;
		case UTF8:
			buf.mark();
			int length = BufferUtils.getUnsignedShort(buf);
			buf.reset();
			info = new byte[length + 2];
			break;
		}
		buf.get(info);
		return this;
	}

}
