package classfile.struct;

import java.nio.ByteBuffer;

import classfile.ClassFormatException;
import util.BufferUtils;

public class AttributeStruct implements Struct<AttributeStruct> {

	public static final String CONSTANT_VALUE = "ConstantValue",
			CODE = "Code",
			STACK_MAP_TABLE = "StackMapTable",
			EXCEPTIONS = "Exceptions",
			INNER_CLASSES = "InnerClasses",
			ENCLOSING_METHOD = "EnclosingMethod",
			SYNTHETIC = "Synthetic",
			SIGNATURE = "Signature",
			SOURCE_FILE = "SourceFile",
			SOURCE_DEBUG_EXTENSION = "SourceDebugExtension",
			LINE_NUMBER_TABLE = "LineNumberTable",
			LOCAL_VARIABLE_TABLE = "LocalVariableTable",
			LOCAL_VARIABLE_TYPE_TABLE = "LocalVariableTypeTable",
			DEPRECATED = "Deprecated",
			RUNTIME_VISIBLE_ANNOTATIONS = "RuntimeVisibleAnnotations",
			RUNTIME_INVISIBLE_ANNOTATIONS = "RuntimeInvisibleAnnotations",
			RUNTIME_VISIBLE_PARAMETER_ANNOTATIONS = "RuntimeVisibleParameterAnnotations",
			RUNTIME_INVISIBLE_PARAMETER_ANNOTATIONS = "RuntimeInvisibleParameterAnnotations",
			ANNOTATION_DEFAULT = "AnnotationDefault",
			BOOTSTRAP_METHODS = "BootstrapMethods";

	public int attributeNameIndex;
	public int attributeLength;
	public byte[] info;

	@Override
	public AttributeStruct read(ByteBuffer buf) {
		attributeNameIndex = BufferUtils.getUnsignedShort(buf);
		attributeLength = buf.getInt();
		if (attributeLength < 0) {
			throw new ClassFormatException("Attribute length greater than Integer.MAX_VALUE!");
		}
		info = new byte[attributeLength];
		buf.get(info);
		return this;
	}
}
