package util;

import java.nio.ByteBuffer;

public final class BufferUtils {
	
	public static int getUnsignedByte(ByteBuffer buf) {
		return (buf.get() & 0xFF);
	}
	
	public static int getUnsignedShort(ByteBuffer buf) {
		return (buf.getShort() & 0xFFFF);
	}
	
	public static int getUnsignedByte(ByteBuffer buf, int pos) {
		return (buf.get(pos) & 0xFF);
	}
	
	public static int getUnsignedShort(ByteBuffer buf, int pos) {
		return (buf.getShort(pos) & 0xFFFF);
	}

}
