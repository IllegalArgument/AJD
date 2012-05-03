package classfile.struct;

import java.nio.ByteBuffer;

import classfile.ClassFormatException;

import util.BufferUtils;

public class ConstantEntryStruct implements Struct<ConstantEntryStruct> {

	public static final int CLASS = 7, FIELD_REF = 9, METHOD_REF = 10,
			INTERFACE_METHOD_REF = 11, STRING = 8, INTEGER = 3, FLOAT = 4,
			LONG = 5, DOUBLE = 6, NAME_AND_TYPE = 12, UTF8 = 1;

	public int tag;
	public ByteBuffer info;

	boolean isDoubleSize() {
		return tag == LONG || tag == DOUBLE;
	}

	@Override
	public ConstantEntryStruct read(ByteBuffer buf) {
		tag = BufferUtils.getUnsignedByte(buf);
		int length;
		switch (tag) {
		case CLASS:
		case STRING:
			length = 2;
			break;
		case FIELD_REF:
		case METHOD_REF:
		case INTERFACE_METHOD_REF:
		case NAME_AND_TYPE:
		case INTEGER:
		case FLOAT:
			length = 4;
			break;
		case LONG:
		case DOUBLE:
			length = 8;
			break;
		case UTF8:
			buf.mark();
			length = BufferUtils.getUnsignedShort(buf) + 2;
			buf.reset();
			break;
		default:
			throw new ClassFormatException("Invalid constant entry tag " + tag + "!");
		}
		info = buf.slice();
		buf.position(buf.position() + length);
		return this;
	}

}
