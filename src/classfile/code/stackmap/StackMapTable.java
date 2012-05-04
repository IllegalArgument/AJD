package classfile.code.stackmap;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import util.BufferUtils;
import util.PrettyPrinter;
import util.Printable;
import classfile.ClassFormatException;
import classfile.ClassReference;
import classfile.JavaClass;
import classfile.struct.AttributeStruct;

public class StackMapTable implements Printable {
	
	private final Map<Integer, StackMapFrame> frames;
	
	public StackMapTable(JavaClass enclosingClass, AttributeStruct struct) {
		ByteBuffer info = struct.info;
		int frameCount = BufferUtils.getUnsignedShort(info);
		Map<Integer, StackMapFrame> frames = new LinkedHashMap<>();
		int offset = 0;
		for (int frame = 0; frame < frameCount; frame++, offset++) {
			int tag = BufferUtils.getUnsignedByte(info);
			switch (tag >> 6) {
			case 0:
				offset += tag;
				frames.put(offset, StackMapFrame.SAME);
				break;
			case 1:
				offset += tag - 64;
				frames.put(offset, new StackMapFrame(FrameType.SAME_LOCALS_1_STACK_ITEM, createVerificationItem(enclosingClass, info)));
				break;
			case 2:
			case 3:
				switch (tag & 0xF) {
				case 7:
					offset += BufferUtils.getUnsignedShort(info);
					frames.put(offset, new StackMapFrame(FrameType.SAME_LOCALS_1_STACK_ITEM_EXTENDED, createVerificationItem(enclosingClass, info)));
					break;
				case 8:
				case 9:
				case 10:
					offset += BufferUtils.getUnsignedShort(info);
					frames.put(offset, new StackMapFrame(FrameType.CHOP, 251 - tag));
					break;
				case 11:
					offset += BufferUtils.getUnsignedShort(info);
					frames.put(offset, StackMapFrame.SAME_EXTENDED);
					break;
				case 12:
				case 13:
				case 14:
					offset += BufferUtils.getUnsignedShort(info);
					int additionalCount = tag - 251;
					List<VerificationItem> newLocals = new ArrayList<>(additionalCount);
					for (int i = 0; i < additionalCount; i++) {
						newLocals.add(createVerificationItem(enclosingClass, info));
					}
					frames.put(offset, new StackMapFrame(FrameType.APPEND, newLocals));
					break;
				case 15:
					offset += BufferUtils.getUnsignedShort(info);
					int localCount = BufferUtils.getUnsignedShort(info);
					List<VerificationItem> locals = new ArrayList<>(localCount);
					for (int i = 0; i < localCount; i++) {
						locals.add(createVerificationItem(enclosingClass, info));
					}
					int stackCount = BufferUtils.getUnsignedShort(info);
					List<VerificationItem> stack = new ArrayList<>(localCount);
					for (int i = 0; i < stackCount; i++) {
						stack.add(createVerificationItem(enclosingClass, info));
					}
					frames.put(offset, new StackMapFrame(FrameType.FULL, new FullFrame(locals, stack)));
					break;
				default:
					throw new ClassFormatException("Stack map frame tag reserved for future use!");
				}
				break;
			default:
				throw new ClassFormatException("Invalid stack map frame tag!");
			}
		}
		this.frames = Collections.unmodifiableMap(frames);
	}
	
	private static final VerificationItem createVerificationItem(JavaClass enclosingClass, ByteBuffer info) {
		int tag = BufferUtils.getUnsignedByte(info);
		switch (tag) {
		case VerificationItem.TOP:
			return VerificationItem.TOP_ITEM;
		case VerificationItem.INTEGER:
			return VerificationItem.INTEGER_ITEM;
		case VerificationItem.FLOAT:
			return VerificationItem.FLOAT_ITEM;
		case VerificationItem.LONG:
			return VerificationItem.LONG_ITEM;
		case VerificationItem.DOUBLE:
			return VerificationItem.DOUBLE_ITEM;
		case VerificationItem.NULL:
			return VerificationItem.NULL_ITEM;
		case VerificationItem.UNINITIALIZED_THIS:
			return VerificationItem.UNINITIALIZED_THIS_ITEM;
		case VerificationItem.OBJECT:
			return new VerificationItem(VerificationType.OBJECT, (ClassReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data);
		case VerificationItem.UNINITIALIZED:
			return new VerificationItem(VerificationType.UNINITIALIZED, BufferUtils.getUnsignedShort(info));
		default:
			throw new ClassFormatException("Invalid verification type tag!");
		}
	}

	@Override
	public void printOn(PrettyPrinter p) {
		p.println("StackMapTable [")
		.indent();
		for (Map.Entry<Integer, StackMapFrame> frame : frames.entrySet()) {
			p.println(frame.getKey() + ": " + frame.getValue()); 
		}
		p.unindent()
		.println("]");
	}

}
