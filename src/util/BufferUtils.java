package util;

import java.nio.ByteBuffer;

/**
 * A utility class for handling {@link ByteBuffer} objects. The methods in this class allow for data types such as unsigned bytes, shorts, and modified UTF-8 strings to be read from a <code>ByteBuffer</code>, similar to the methods provided by {@link DataInputStream}.
 * 
 * @author Aaron Willey
 * @version 0.1
 */
public final class BufferUtils {
	
	/**
	 * Gets an unsigned byte from the <code>ByteBuffer</code>.
	 * 
	 * @param buf the <code>ByteBuffer</code> from which the unsigned byte is to be read
	 * @return the unsigned byte read from the <code>ByteBuffer</code>
	 */
	public static int getUnsignedByte(ByteBuffer buf) {
		return (buf.get() & 0xFF);
	}

	/**
	 * Gets an unsigned short from the <code>ByteBuffer</code>.
	 * 
	 * @param buf the <code>ByteBuffer</code> from which the unsigned short is to be read
	 * @return the unsigned short read from the <code>ByteBuffer</code>
	 */
	public static int getUnsignedShort(ByteBuffer buf) {
		return (buf.getShort() & 0xFFFF);
	}
	
	/**
	 * Gets a string encoded in modified UTF-8 format from the <code>ByteBuffer</code>.
	 * 
	 * <p>
	 * This method reads a string encoded in the same format used in the Java class file format - a modified UTF-8 format, defined in the Java Virtual Machine Specification. 
	 * </p>
	 * 
	 * @param buf the <code>ByteBuffer</code> from which the modified UTF-8 string is to be read
	 * @return the string read from the <code>ByteBuffer</code>
	 */
	public static String getModifiedUTF8(ByteBuffer buf) {
		//Much of this method is adapted from the analogous method in DataInputStream
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
