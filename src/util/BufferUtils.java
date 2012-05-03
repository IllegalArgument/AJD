package util;

import java.nio.ByteBuffer;

public final class BufferUtils {
	
	public static int getUnsignedByte(ByteBuffer buf) {
		return (buf.get() & 0xFF);
	}
	
	public static int getUnsignedShort(ByteBuffer buf) {
		return (buf.getShort() & 0xFFFF);
	}
	
	public static String getModifiedUTF8(ByteBuffer buf) {
		int length = getUnsignedShort(buf);
		buf.limit(buf.position() + length);
		char[] result = new char[length];
		int charPos;
		for (charPos = 0; buf.hasRemaining(); charPos++) {
			int c = buf.get() & 0xFF;
			switch (c >> 4) {
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				result[charPos] = (char) c;
				break;
			case 12:
			case 13:
				result[charPos] = (char) (((c & 0x1F) << 6) | (buf.get() & 0x3F));
				break;
			case 14:
				result[charPos] = (char) (((c & 0x0F) << 12) | ((buf.get() & 0x3F) << 6) | (buf.get() & 0x3F));
				break;
			default:
				return null;
			}
		}
		return new String(result, 0, charPos);
	}

}
