package classfile.struct;

import java.nio.ByteBuffer;

public interface Struct<T> {
	
	T read(ByteBuffer buf);

}
