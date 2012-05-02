package classfile.code;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import classfile.ClassFormatException;
import classfile.ClassReference;
import classfile.ConstantEntry;
import classfile.ConstantType;
import classfile.FieldReference;
import classfile.JavaClass;
import classfile.MethodReference;
import classfile.Primitive;
import classfile.struct.AttributeStruct;
import util.BufferUtils;
import util.PrettyPrinter;
import util.Printable;

public class Code implements Printable {

	public final int maxStack;
	public final int maxLocals;
	public final List<Opcode> ops;
	public final List<ExceptionHandler> exceptionTable;
	public final List<ClassReference> exceptions;

	public Code(JavaClass enclosingClass, AttributeStruct struct) {
		if (!enclosingClass.getConstant(struct.attributeNameIndex).data.equals(AttributeStruct.CODE)) {
			throw new ClassFormatException("Attribute name for Code attribute must be 'Code'!");
		}
		ByteBuffer info = ByteBuffer.wrap(struct.info);
		maxStack = BufferUtils.getUnsignedShort(info);
		maxLocals = BufferUtils.getUnsignedShort(info);
		int codeLength = info.getInt();
		if (codeLength < 0) {
			throw new ClassFormatException("Code length greater than Integer.MAX_VALUE!");
		}
		List<Opcode> ops = new ArrayList<>(codeLength);
		int codeStart = info.position();
		info.limit(codeStart + codeLength);
		int pos = 0;
		while (info.remaining() > 0) {
			switch (BufferUtils.getUnsignedByte(info)) {
			case NOP:
				ops.add(new Opcode(OpType.NOOP, null));
				break;
			case ACONST_NULL:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, ConstantEntry.NULL));
				break;
			case ICONST_M1:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, -1)));
				break;
			case ICONST_0:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, 0)));
				break;
			case ICONST_1:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, 1)));
				break;
			case ICONST_2:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, 2)));
				break;
			case ICONST_3:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, 3)));
				break;
			case ICONST_4:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, 4)));
				break;
			case ICONST_5:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, 5)));
				break;
			case LCONST_0:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.LONG, 0L)));
				break;
			case LCONST_1:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.LONG, 1L)));
				break;
			case FCONST_0:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.FLOAT, 0.0F)));
				break;
			case FCONST_1:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.FLOAT, 1.0F)));
				break;
			case FCONST_2:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.FLOAT, 2.0F)));
				break;
			case DCONST_0:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.DOUBLE, 0.0)));
				break;
			case DCONST_1:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.DOUBLE, 1.0)));
				break;
			case BIPUSH:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, info.get())));
				break;
			case SIPUSH:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, new ConstantEntry(ConstantType.INTEGER, info.getShort())));
				break;
			case LDC:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, enclosingClass.getConstant(BufferUtils.getUnsignedByte(info))));
				break;
			case LDC_W:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, enclosingClass.getConstant(BufferUtils.getUnsignedShort(info))));
				break;
			case LDC2_W:
				ops.add(new Opcode(OpType.CONSTANT_LOAD, enclosingClass.getConstant(BufferUtils.getUnsignedShort(info))));
				break;
			case ILOAD:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.INT)));
				break;
			case LLOAD:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.LONG)));
				break;
			case FLOAD:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.FLOAT)));
				break;
			case DLOAD:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.DOUBLE)));
				break;
			case ALOAD:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.REFERENCE)));
				break;
			case ILOAD_0:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(0, ComputationalType.INT)));
				break;
			case ILOAD_1:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(1, ComputationalType.INT)));
				break;
			case ILOAD_2:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(2, ComputationalType.INT)));
				break;
			case ILOAD_3:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(3, ComputationalType.INT)));
				break;
			case LLOAD_0:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(0, ComputationalType.LONG)));
				break;
			case LLOAD_1:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(1, ComputationalType.LONG)));
				break;
			case LLOAD_2:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(2, ComputationalType.LONG)));
				break;
			case LLOAD_3:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(3, ComputationalType.LONG)));
				break;
			case FLOAD_0:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(0, ComputationalType.FLOAT)));
				break;
			case FLOAD_1:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(1, ComputationalType.FLOAT)));
				break;
			case FLOAD_2:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(2, ComputationalType.FLOAT)));
				break;
			case FLOAD_3:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(3, ComputationalType.FLOAT)));
				break;
			case DLOAD_0:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(0, ComputationalType.DOUBLE)));
				break;
			case DLOAD_1:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(1, ComputationalType.DOUBLE)));
				break;
			case DLOAD_2:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(2, ComputationalType.DOUBLE)));
				break;
			case DLOAD_3:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(3, ComputationalType.DOUBLE)));
				break;
			case ALOAD_0:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(0, ComputationalType.REFERENCE)));
				break;
			case ALOAD_1:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(1, ComputationalType.REFERENCE)));
				break;
			case ALOAD_2:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(2, ComputationalType.REFERENCE)));
				break;
			case ALOAD_3:
				ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(3, ComputationalType.REFERENCE)));
				break;
			case IALOAD:
				ops.add(new Opcode(OpType.ARRAY_LOAD, Primitive.INT));
				break;
			case LALOAD:
				ops.add(new Opcode(OpType.ARRAY_LOAD, Primitive.LONG));
				break;
			case FALOAD:
				ops.add(new Opcode(OpType.ARRAY_LOAD, Primitive.FLOAT));
				break;
			case DALOAD:
				ops.add(new Opcode(OpType.ARRAY_LOAD, Primitive.DOUBLE));
				break;
			case AALOAD:
				ops.add(new Opcode(OpType.ARRAY_LOAD, Primitive.REFERENCE));
				break;
			case BALOAD:
				ops.add(new Opcode(OpType.ARRAY_LOAD, Primitive.BYTE));
				break;
			case CALOAD:
				ops.add(new Opcode(OpType.ARRAY_LOAD, Primitive.CHAR));
				break;
			case SALOAD:
				ops.add(new Opcode(OpType.ARRAY_LOAD, Primitive.SHORT));
				break;
			case ISTORE:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.INT)));
				break;
			case LSTORE:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.LONG)));
				break;
			case FSTORE:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.FLOAT)));
				break;
			case DSTORE:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.DOUBLE)));
				break;
			case ASTORE:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.REFERENCE)));
				break;
			case ISTORE_0:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(0, ComputationalType.INT)));
				break;
			case ISTORE_1:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(1, ComputationalType.INT)));
				break;
			case ISTORE_2:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(2, ComputationalType.INT)));
				break;
			case ISTORE_3:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(3, ComputationalType.INT)));
				break;
			case LSTORE_0:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(0, ComputationalType.LONG)));
				break;
			case LSTORE_1:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(1, ComputationalType.LONG)));
				break;
			case LSTORE_2:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(2, ComputationalType.LONG)));
				break;
			case LSTORE_3:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(3, ComputationalType.LONG)));
				break;
			case FSTORE_0:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(0, ComputationalType.FLOAT)));
				break;
			case FSTORE_1:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(1, ComputationalType.FLOAT)));
				break;
			case FSTORE_2:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(2, ComputationalType.FLOAT)));
				break;
			case FSTORE_3:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(3, ComputationalType.FLOAT)));
				break;
			case DSTORE_0:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(0, ComputationalType.DOUBLE)));
				break;
			case DSTORE_1:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(1, ComputationalType.DOUBLE)));
				break;
			case DSTORE_2:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(2, ComputationalType.DOUBLE)));
				break;
			case DSTORE_3:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(3, ComputationalType.DOUBLE)));
				break;
			case ASTORE_0:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(0, ComputationalType.REFERENCE)));
				break;
			case ASTORE_1:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(1, ComputationalType.REFERENCE)));
				break;
			case ASTORE_2:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(2, ComputationalType.REFERENCE)));
				break;
			case ASTORE_3:
				ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(3, ComputationalType.REFERENCE)));
				break;
			case IASTORE:
				ops.add(new Opcode(OpType.ARRAY_STORE, Primitive.INT));
				break;
			case LASTORE:
				ops.add(new Opcode(OpType.ARRAY_STORE, Primitive.LONG));
				break;
			case FASTORE:
				ops.add(new Opcode(OpType.ARRAY_STORE, Primitive.FLOAT));
				break;
			case DASTORE:
				ops.add(new Opcode(OpType.ARRAY_STORE, Primitive.DOUBLE));
				break;
			case AASTORE:
				ops.add(new Opcode(OpType.ARRAY_STORE, Primitive.REFERENCE));
				break;
			case BASTORE:
				ops.add(new Opcode(OpType.ARRAY_STORE, Primitive.BYTE));
				break;
			case CASTORE:
				ops.add(new Opcode(OpType.ARRAY_STORE, Primitive.CHAR));
				break;
			case SASTORE:
				ops.add(new Opcode(OpType.ARRAY_STORE, Primitive.SHORT));
				break;
			case POP:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.POP));
				break;
			case POP2:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.POP2));
				break;
			case DUP:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.DUP));
				break;
			case DUP_X1:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.DUP_X1));
				break;
			case DUP_X2:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.DUP_X2));
				break;
			case DUP2:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.DUP2));
				break;
			case DUP2_X1:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.DUP2_X1));
				break;
			case DUP2_X2:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.DUP2_X2));
				break;
			case SWAP:
				ops.add(new Opcode(OpType.STACK_MANAGE, StackManagement.SWAP));
				break;
			case IADD:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.ADD, ComputationalType.INT)));
				break;
			case LADD:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.ADD, ComputationalType.LONG)));
				break;
			case FADD:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.ADD, ComputationalType.FLOAT)));
				break;
			case DADD:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.ADD, ComputationalType.DOUBLE)));
				break;
			case ISUB:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.SUBTRACT, ComputationalType.INT)));
				break;
			case LSUB:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.SUBTRACT, ComputationalType.LONG)));
				break;
			case FSUB:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.SUBTRACT, ComputationalType.FLOAT)));
				break;
			case DSUB:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.SUBTRACT, ComputationalType.DOUBLE)));
				break;
			case IMUL:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.MULTIPLY, ComputationalType.INT)));
				break;
			case LMUL:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.MULTIPLY, ComputationalType.LONG)));
				break;
			case FMUL:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.MULTIPLY, ComputationalType.FLOAT)));
				break;
			case DMUL:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.MULTIPLY, ComputationalType.DOUBLE)));
				break;
			case IDIV:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.DIVIDE, ComputationalType.INT)));
				break;
			case LDIV:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.DIVIDE, ComputationalType.LONG)));
				break;
			case FDIV:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.DIVIDE, ComputationalType.FLOAT)));
				break;
			case DDIV:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.DIVIDE, ComputationalType.DOUBLE)));
				break;
			case IREM:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.REMAINDER, ComputationalType.INT)));
				break;
			case LREM:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.REMAINDER, ComputationalType.LONG)));
				break;
			case FREM:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.REMAINDER, ComputationalType.FLOAT)));
				break;
			case DREM:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.REMAINDER, ComputationalType.DOUBLE)));
				break;
			case INEG:
				ops.add(new Opcode(OpType.NEGATE, ComputationalType.INT));
				break;
			case LNEG:
				ops.add(new Opcode(OpType.NEGATE, ComputationalType.LONG));
				break;
			case FNEG:
				ops.add(new Opcode(OpType.NEGATE, ComputationalType.FLOAT));
				break;
			case DNEG:
				ops.add(new Opcode(OpType.NEGATE, ComputationalType.DOUBLE));
				break;
			case ISHL:
				ops.add(new Opcode(OpType.SHIFT, new Shift(ShiftType.SHIFT_LEFT, ComputationalType.INT)));
				break;
			case LSHL:
				ops.add(new Opcode(OpType.SHIFT, new Shift(ShiftType.SHIFT_LEFT, ComputationalType.LONG)));
				break;
			case ISHR:
				ops.add(new Opcode(OpType.SHIFT, new Shift(ShiftType.SHIFT_RIGHT, ComputationalType.INT)));
				break;
			case LSHR:
				ops.add(new Opcode(OpType.SHIFT, new Shift(ShiftType.SHIFT_RIGHT, ComputationalType.LONG)));
				break;
			case IUSHR:
				ops.add(new Opcode(OpType.SHIFT, new Shift(ShiftType.LOGICAL_SHIFT_RIGHT, ComputationalType.INT)));
				break;
			case LUSHR:
				ops.add(new Opcode(OpType.SHIFT, new Shift(ShiftType.LOGICAL_SHIFT_RIGHT, ComputationalType.LONG)));
				break;
			case IAND:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.AND, ComputationalType.INT)));
				break;
			case LAND:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.AND, ComputationalType.LONG)));
				break;
			case IOR:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.OR, ComputationalType.INT)));
				break;
			case LOR:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.OR, ComputationalType.LONG)));
				break;
			case IXOR:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.XOR, ComputationalType.INT)));
				break;
			case LXOR:
				ops.add(new Opcode(OpType.ARITHMETIC, new Arithmetic(ArithmeticType.XOR, ComputationalType.LONG)));
				break;
			case IINC:
				ops.add(new Opcode(OpType.LOCAL_INCREMENT, new LocalVariableIncrement(new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.INT), info.get())));
				break;
			case I2L:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.INT, Primitive.LONG)));
				break;
			case I2F:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.INT, Primitive.FLOAT)));
				break;
			case I2D:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.INT, Primitive.DOUBLE)));
				break;
			case L2I:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.LONG, Primitive.INT)));
				break;
			case L2F:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.LONG, Primitive.FLOAT)));
				break;
			case L2D:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.LONG, Primitive.DOUBLE)));
				break;
			case F2I:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.FLOAT, Primitive.INT)));
				break;
			case F2L:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.FLOAT, Primitive.LONG)));
				break;
			case F2D:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.FLOAT, Primitive.DOUBLE)));
				break;
			case D2I:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.DOUBLE, Primitive.INT)));
				break;
			case D2L:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.DOUBLE, Primitive.LONG)));
				break;
			case D2F:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.DOUBLE, Primitive.FLOAT)));
				break;
			case I2B:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.INT, Primitive.BYTE)));
				break;
			case I2C:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.INT, Primitive.CHAR)));
				break;
			case I2S:
				ops.add(new Opcode(OpType.CONVERT, new Conversion(Primitive.INT, Primitive.SHORT)));
				break;
			case LCMP:
				ops.add(new Opcode(OpType.COMPARE, new Comparison(ComputationalType.LONG, CompareOption.NORMAL)));
				break;
			case FCMPL:
				ops.add(new Opcode(OpType.COMPARE, new Comparison(ComputationalType.FLOAT, CompareOption.LESS_DEFAULT)));
				break;
			case FCMPG:
				ops.add(new Opcode(OpType.COMPARE, new Comparison(ComputationalType.FLOAT, CompareOption.GREATER_DEFAULT)));
				break;
			case DCMPL:
				ops.add(new Opcode(OpType.COMPARE, new Comparison(ComputationalType.DOUBLE, CompareOption.LESS_DEFAULT)));
				break;
			case DCMPG:
				ops.add(new Opcode(OpType.COMPARE, new Comparison(ComputationalType.DOUBLE, CompareOption.GREATER_DEFAULT)));
				break;
			case IFEQ:
				ops.add(new Opcode(OpType.CONDITIONAL_JUMP, new ConditionalJump(JumpCondition.EQUAL_ZERO, ComputationalType.INT, pos + info.getShort())));
				break;
			case IFNE:
				ops.add(new Opcode(OpType.CONDITIONAL_JUMP, new ConditionalJump(JumpCondition.NOT_EQUAL_ZERO, ComputationalType.INT, pos + info.getShort())));
				break;
			case IFLT:
				ops.add(new Opcode(OpType.CONDITIONAL_JUMP, new ConditionalJump(JumpCondition.LESS_ZERO, ComputationalType.INT, pos + info.getShort())));
				break;
			case IFGE:
				ops.add(new Opcode(OpType.CONDITIONAL_JUMP, new ConditionalJump(JumpCondition.GREATER_EQUAL_ZERO, ComputationalType.INT, pos + info.getShort())));
				break;
			case IFGT:
				ops.add(new Opcode(OpType.CONDITIONAL_JUMP, new ConditionalJump(JumpCondition.GREATER_ZERO, ComputationalType.INT, pos + info.getShort())));
				break;
			case IFLE:
				ops.add(new Opcode(OpType.CONDITIONAL_JUMP, new ConditionalJump(JumpCondition.LESS_EQUAL_ZERO, ComputationalType.INT, pos + info.getShort())));
				break;
			case IF_ICMPEQ:
				ops.add(new Opcode(OpType.COMPARE_JUMP, new CompareJump(CompareCondition.COMPARE_EQUAL, ComputationalType.INT, pos + info.getShort())));
				break;
			case IF_ICMPNE:
				ops.add(new Opcode(OpType.COMPARE_JUMP, new CompareJump(CompareCondition.COMPARE_NOT_EQUAL, ComputationalType.INT, pos + info.getShort())));
				break;
			case IF_ICMPLT:
				ops.add(new Opcode(OpType.COMPARE_JUMP, new CompareJump(CompareCondition.COMPARE_LESS, ComputationalType.INT, pos + info.getShort())));
				break;
			case IF_ICMPGE:
				ops.add(new Opcode(OpType.COMPARE_JUMP, new CompareJump(CompareCondition.COMPARE_GREATER_EQUAL, ComputationalType.INT, pos + info.getShort())));
				break;
			case IF_ICMPGT:
				ops.add(new Opcode(OpType.COMPARE_JUMP, new CompareJump(CompareCondition.COMPARE_GREATER, ComputationalType.INT, pos + info.getShort())));
				break;
			case IF_ICMPLE:
				ops.add(new Opcode(OpType.COMPARE_JUMP, new CompareJump(CompareCondition.COMPARE_LESS_EQUAL, ComputationalType.INT, pos + info.getShort())));
				break;
			case IF_ACMPEQ:
				ops.add(new Opcode(OpType.COMPARE_JUMP, new CompareJump(CompareCondition.COMPARE_EQUAL, ComputationalType.REFERENCE, pos + info.getShort())));
				break;
			case IF_ACMPNE:
				ops.add(new Opcode(OpType.COMPARE_JUMP, new CompareJump(CompareCondition.COMPARE_NOT_EQUAL, ComputationalType.INT, pos + info.getShort())));
				break;
			case GOTO:
				ops.add(new Opcode(OpType.UNCONDITIONAL_JUMP, pos + info.getShort()));
				break;
			case JSR:
				ops.add(new Opcode(OpType.SUBROUTINE_JUMP, pos + info.getShort()));
				break;
			case RET:
				ops.add(new Opcode(OpType.SUBROUTINE_RETURN, new LocalVariable(BufferUtils.getUnsignedByte(info), ComputationalType.RETURN_ADDRESS)));
				break;
			case TABLESWITCH:
			{
				byte[] pad = new byte[3 - (pos % 4)];
				info.get(pad);
				int defaultJump = pos + info.getInt();
				int low = info.getInt();
				int high = info.getInt();
				Map<Integer, Integer> jumpTable = new HashMap<>(high - low + 1);
				for (int i = low; i <= high; i++) {
					jumpTable.put(i, pos + info.getInt());
				}
				ops.add(new Opcode(OpType.SWITCH, new Switch(jumpTable, defaultJump)));
				break;
			}
			case LOOKUPSWITCH:
			{
				byte[] pad = new byte[3 - (pos % 4)];
				info.get(pad);
				int defaultJump = pos + info.getInt();
				int nPairs = info.getInt();
				Map<Integer, Integer> jumpTable = new HashMap<>(nPairs);
				for (int i = 0; i < nPairs; i++) {
					jumpTable.put(info.getInt(), pos + info.getInt());
				}
				ops.add(new Opcode(OpType.SWITCH, new Switch(jumpTable, defaultJump)));
				break;
			}
			case IRETURN:
				ops.add(new Opcode(OpType.RETURN, ComputationalType.INT));
				break;
			case LRETURN:
				ops.add(new Opcode(OpType.RETURN, ComputationalType.LONG));
				break;
			case FRETURN:
				ops.add(new Opcode(OpType.RETURN, ComputationalType.FLOAT));
				break;
			case DRETURN:
				ops.add(new Opcode(OpType.RETURN, ComputationalType.DOUBLE));
				break;
			case ARETURN:
				ops.add(new Opcode(OpType.RETURN, ComputationalType.REFERENCE));
				break;
			case RETURN:
				ops.add(new Opcode(OpType.RETURN, ComputationalType.VOID));
				break;
			case GETSTATIC:
				ops.add(new Opcode(OpType.FIELD_LOAD, new FieldAccessor((FieldReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, true)));
				break;
			case PUTSTATIC:
				ops.add(new Opcode(OpType.FIELD_STORE, new FieldAccessor((FieldReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, true)));
				break;
			case GETFIELD:
				ops.add(new Opcode(OpType.FIELD_LOAD, new FieldAccessor((FieldReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, false)));
				break;
			case PUTFIELD:
				ops.add(new Opcode(OpType.FIELD_STORE, new FieldAccessor((FieldReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, false)));
				break;
			case INVOKEVIRTUAL:
				ops.add(new Opcode(OpType.METHOD_INVOKE, new MethodInvocation((MethodReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, MethodType.VIRTUAL)));
				break;
			case INVOKESPECIAL:
				ops.add(new Opcode(OpType.METHOD_INVOKE, new MethodInvocation((MethodReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, MethodType.SPECIAL)));
				break;
			case INVOKESTATIC:
				ops.add(new Opcode(OpType.METHOD_INVOKE, new MethodInvocation((MethodReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, MethodType.STATIC)));
				break;
			case INVOKEINTERFACE:
				ops.add(new Opcode(OpType.METHOD_INVOKE, new MethodInvocation((MethodReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, MethodType.INTERFACE)));
				info.getShort();
				break;
			case NEW:
				ops.add(new Opcode(OpType.NEW_OBJECT, enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data));
				break;
			case NEWARRAY:
			{
				Primitive type;
				switch (BufferUtils.getUnsignedByte(info)) {
				case ArrayInstantiation.T_BOOLEAN:
					type = Primitive.BOOLEAN;
					break;
				case ArrayInstantiation.T_CHAR:
					type = Primitive.CHAR;
					break;
				case ArrayInstantiation.T_FLOAT:
					type = Primitive.FLOAT;
					break;
				case ArrayInstantiation.T_DOUBLE:
					type = Primitive.DOUBLE;
					break;
				case ArrayInstantiation.T_BYTE:
					type = Primitive.BYTE;
					break;
				case ArrayInstantiation.T_SHORT:
					type = Primitive.SHORT;
					break;
				case ArrayInstantiation.T_INT:
					type = Primitive.INT;
					break;
				case ArrayInstantiation.T_LONG:
					type = Primitive.LONG;
					break;
				default:
					throw new ClassFormatException("Invalid type specified for newarry op!");
				}
				ops.add(new Opcode(OpType.NEW_ARRAY, new ArrayInstantiation(ClassReference.arrayFromPrimitive(type, 1), 1)));
				break;
			}
			case ANEWARRAY:
				ops.add(new Opcode(OpType.NEW_ARRAY, new ArrayInstantiation(ClassReference.arrayFromElementType((ClassReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data), 1)));
				break;
			case ARRAYLENGTH:
				ops.add(new Opcode(OpType.ARRAY_LENGTH, null));
				break;
			case ATHROW:
				ops.add(new Opcode(OpType.THROW, null));
				break;
			case CHECKCAST:
				ops.add(new Opcode(OpType.CAST, (ClassReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data));
				break;
			case INSTANCEOF:
				ops.add(new Opcode(OpType.INSTANCE_OF, (ClassReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data));
				break;
			case MONITORENTER:
				ops.add(new Opcode(OpType.SYNCHRONIZE, true));
				break;
			case MONITOREXIT:
				ops.add(new Opcode(OpType.SYNCHRONIZE, false));
				break;
			case WIDE:
			{
				switch (BufferUtils.getUnsignedByte(info)) {
				case ILOAD:
					ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.INT)));
					break;
				case LLOAD:
					ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.LONG)));
					break;
				case FLOAD:
					ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.FLOAT)));
					break;
				case DLOAD:
					ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.DOUBLE)));
					break;
				case ALOAD:
					ops.add(new Opcode(OpType.LOCAL_LOAD, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.REFERENCE)));
					break;
				case ISTORE:
					ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.INT)));
					break;
				case LSTORE:
					ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.LONG)));
					break;
				case FSTORE:
					ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.FLOAT)));
					break;
				case DSTORE:
					ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.DOUBLE)));
					break;
				case ASTORE:
					ops.add(new Opcode(OpType.LOCAL_STORE, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.REFERENCE)));
					break;
				case RET:
					ops.add(new Opcode(OpType.SUBROUTINE_RETURN, new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.RETURN_ADDRESS)));
					break;
				case IINC:
					ops.add(new Opcode(OpType.LOCAL_INCREMENT, new LocalVariableIncrement(new LocalVariable(BufferUtils.getUnsignedShort(info), ComputationalType.INT), info.getShort())));
					break;
				default:
					throw new ClassFormatException("Invalid opcode encountered in wide op!");
				}
				break;
			}
			case MULTIANEWARRAY:
				ops.add(new Opcode(OpType.NEW_ARRAY, new ArrayInstantiation((ClassReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data, BufferUtils.getUnsignedByte(info))));
				break;
			case IFNULL:
				ops.add(new Opcode(OpType.CONDITIONAL_JUMP, new ConditionalJump(JumpCondition.IS_NULL, ComputationalType.REFERENCE, pos + info.getShort())));
				break;
			case IFNONNULL:
				ops.add(new Opcode(OpType.CONDITIONAL_JUMP, new ConditionalJump(JumpCondition.IS_NOT_NULL, ComputationalType.REFERENCE, pos + info.getShort())));
				break;
			case GOTO_W:
				ops.add(new Opcode(OpType.UNCONDITIONAL_JUMP, pos + info.getInt()));
				break;
			case JSR_W:
				ops.add(new Opcode(OpType.SUBROUTINE_JUMP, pos + info.getInt()));
				break;
			default:
				throw new ClassFormatException("Unknown opcode encountered at index " + pos + "!");
			}
			int nextPos = info.position() - codeStart;
			for (int i = pos + 1; i < nextPos; i++) {
				ops.add(null);
			}
			pos = nextPos;
		}
		info.limit(info.capacity());
		this.ops = Collections.unmodifiableList(ops);
		int exceptionTableLength = BufferUtils.getUnsignedShort(info);
		List<ExceptionHandler> exceptionTable = new ArrayList<>();
		for (int i = 0; i < exceptionTableLength; i++) {
			exceptionTable.add(new ExceptionHandler(BufferUtils.getUnsignedShort(info), BufferUtils.getUnsignedShort(info),
					BufferUtils.getUnsignedShort(info), (ClassReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(info)).data));
		}
		this.exceptionTable = Collections.unmodifiableList(exceptionTable);
		List<ClassReference> exceptions = new LinkedList<>();
		int attributesCount = BufferUtils.getUnsignedShort(info);
		for (int i = 0; i < attributesCount; i++) {
			AttributeStruct attribute = new AttributeStruct().read(info);
			String attributeName = (String) enclosingClass.getConstant(attribute.attributeNameIndex).data;
			if (attributeName.equals(AttributeStruct.EXCEPTIONS)) {
				ByteBuffer attributeInfo = ByteBuffer.wrap(attribute.info);
				int numberOfExceptions = BufferUtils.getUnsignedShort(attributeInfo);
				for (int j = 0; j < numberOfExceptions; j++) {
					exceptions.add((ClassReference) enclosingClass.getConstant(BufferUtils.getUnsignedShort(attributeInfo)).data);
				}
			}
		}
		this.exceptions = Collections.unmodifiableList(exceptions);
	}
	
	@Override
	public void printOn(PrettyPrinter p) {
		p.println("Code [")
		.indent()
		.println("Max Stack: " + maxStack)
		.println("Max Locals: " + maxLocals)
		.println("Ops [")
		.indent();
		for (int i = 0; i < ops.size(); i++) {
			Opcode op;
			if ((op = ops.get(i)) == null) {
				continue;
			}
			p.println(i + ": " + op);
		}
		p.unindent()
		.println("]")
		.println("Exception Handlers [")
		.indent();
		for (ExceptionHandler handler : exceptionTable) {
			p.println(handler.toString());
		}
		p.unindent()
		.println("]")
		.println("Exceptions Thrown [")
		.indent();
		for (ClassReference exception : exceptions) {
			p.println(exception.toString());
		}
		p.unindent()
		.println("]")
		.unindent()
		.println("]");
	}

	private static final int NOP = 0x00, ACONST_NULL = 0x01, ICONST_M1 = 0x02,
			ICONST_0 = 0x03, ICONST_1 = 0x04, ICONST_2 = 0x05, ICONST_3 = 0x06,
			ICONST_4 = 0x07, ICONST_5 = 0x08, LCONST_0 = 0x09, LCONST_1 = 0x0A,
			FCONST_0 = 0x0B, FCONST_1 = 0x0C, FCONST_2 = 0x0D, DCONST_0 = 0x0E,
			DCONST_1 = 0x0F, BIPUSH = 0x10, SIPUSH = 0x11, LDC = 0x12,
			LDC_W = 0x13, LDC2_W = 0x14, ILOAD = 0x15, LLOAD = 0x16,
			FLOAD = 0x17, DLOAD = 0x18, ALOAD = 0x19, ILOAD_0 = 0x1A,
			ILOAD_1 = 0x1B, ILOAD_2 = 0x1C, ILOAD_3 = 0x1D, LLOAD_0 = 0x1E,
			LLOAD_1 = 0x1F, LLOAD_2 = 0x20, LLOAD_3 = 0x21, FLOAD_0 = 0x22,
			FLOAD_1 = 0x23, FLOAD_2 = 0x24, FLOAD_3 = 0x25, DLOAD_0 = 0x26,
			DLOAD_1 = 0x27, DLOAD_2 = 0x28, DLOAD_3 = 0x29, ALOAD_0 = 0x2A,
			ALOAD_1 = 0x2B, ALOAD_2 = 0x2C, ALOAD_3 = 0x2D, IALOAD = 0x2E,
			LALOAD = 0x2F, FALOAD = 0x30, DALOAD = 0x31, AALOAD = 0x32,
			BALOAD = 0x33, CALOAD = 0x34, SALOAD = 0x35, ISTORE = 0x36,
			LSTORE = 0x37, FSTORE = 0x38, DSTORE = 0x39, ASTORE = 0x3A,
			ISTORE_0 = 0x3B, ISTORE_1 = 0x3C, ISTORE_2 = 0x3D, ISTORE_3 = 0x3E,
			LSTORE_0 = 0x3F, LSTORE_1 = 0x40, LSTORE_2 = 0x41, LSTORE_3 = 0x42,
			FSTORE_0 = 0x43, FSTORE_1 = 0x44, FSTORE_2 = 0x45, FSTORE_3 = 0x46,
			DSTORE_0 = 0x47, DSTORE_1 = 0x48, DSTORE_2 = 0x49, DSTORE_3 = 0x4A,
			ASTORE_0 = 0x4B, ASTORE_1 = 0x4C, ASTORE_2 = 0x4D, ASTORE_3 = 0x4E,
			IASTORE = 0x4F, LASTORE = 0x50, FASTORE = 0x51, DASTORE = 0x52,
			AASTORE = 0x53, BASTORE = 0x54, CASTORE = 0x55, SASTORE = 0x56,
			POP = 0x57, POP2 = 0x58, DUP = 0x59, DUP_X1 = 0x5A, DUP_X2 = 0x5B,
			DUP2 = 0x5C, DUP2_X1 = 0x5D, DUP2_X2 = 0x5E, SWAP = 0x5F,
			IADD = 0x60, LADD = 0x61, FADD = 0x62, DADD = 0x63, ISUB = 0x64,
			LSUB = 0x65, FSUB = 0x66, DSUB = 0x67, IMUL = 0x68, LMUL = 0x69,
			FMUL = 0x6A, DMUL = 0x6B, IDIV = 0x6C, LDIV = 0x6D, FDIV = 0x6E,
			DDIV = 0x6F, IREM = 0x70, LREM = 0x71, FREM = 0x72, DREM = 0x73,
			INEG = 0x74, LNEG = 0x75, FNEG = 0x76, DNEG = 0x77, ISHL = 0x78,
			LSHL = 0x79, ISHR = 0x7A, LSHR = 0x7B, IUSHR = 0x7C, LUSHR = 0x7D,
			IAND = 0x7E, LAND = 0x7F, IOR = 0x80, LOR = 0x81, IXOR = 0x82,
			LXOR = 0x83, IINC = 0x84, I2L = 0x85, I2F = 0x86, I2D = 0x87,
			L2I = 0x88, L2F = 0x89, L2D = 0x8A, F2I = 0x8B, F2L = 0x8C,
			F2D = 0x8D, D2I = 0x8E, D2L = 0x8F, D2F = 0x90, I2B = 0x91,
			I2C = 0x92, I2S = 0x93, LCMP = 0x94, FCMPL = 0x95, FCMPG = 0x96,
			DCMPL = 0x97, DCMPG = 0x98, IFEQ = 0x99, IFNE = 0x9A, IFLT = 0x9B,
			IFGE = 0x9C, IFGT = 0x9D, IFLE = 0x9E, IF_ICMPEQ = 0x9F,
			IF_ICMPNE = 0xA0, IF_ICMPLT = 0xA1, IF_ICMPGE = 0xA2,
			IF_ICMPGT = 0xA3, IF_ICMPLE = 0xA4, IF_ACMPEQ = 0xA5,
			IF_ACMPNE = 0xA6, GOTO = 0xA7, JSR = 0xA8, RET = 0xA9,
			TABLESWITCH = 0xAA, LOOKUPSWITCH = 0xAB, IRETURN = 0xAC,
			LRETURN = 0xAD, FRETURN = 0xAE, DRETURN = 0xAF, ARETURN = 0xB0,
			RETURN = 0xB1, GETSTATIC = 0xB2, PUTSTATIC = 0xB3, GETFIELD = 0xB4,
			PUTFIELD = 0xB5, INVOKEVIRTUAL = 0xB6, INVOKESPECIAL = 0xB7,
			INVOKESTATIC = 0xB8, INVOKEINTERFACE = 0xB9, NEW = 0xBB,
			NEWARRAY = 0xBC, ANEWARRAY = 0xBD, ARRAYLENGTH = 0xBE,
			ATHROW = 0xBF, CHECKCAST = 0xC0, INSTANCEOF = 0xC1,
			MONITORENTER = 0xC2, MONITOREXIT = 0xC3, WIDE = 0xC4,
			MULTIANEWARRAY = 0xC5, IFNULL = 0xC6, IFNONNULL = 0xC7,
			GOTO_W = 0xC8, JSR_W = 0xC9;

}
