package analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import util.PrettyPrinter;
import analysis.data.ArrayAccess;
import analysis.data.Assignment;
import analysis.data.BinaryArithmetic;
import analysis.data.Cast;
import analysis.data.Compare;
import analysis.data.ComparisonType;
import analysis.data.Convert;
import analysis.data.DataBlock;
import analysis.data.DataConnection;
import analysis.data.DataTransition;
import analysis.data.FieldAccess;
import analysis.data.InstanceOf;
import analysis.data.MethodCall;
import analysis.data.NewArray;
import analysis.data.ShiftArithmetic;
import analysis.data.Value;
import analysis.data.ValueComparison;
import analysis.data.ValueType;
import analysis.flow.BasicBlock;
import classfile.ClassReference;
import classfile.ConstantEntry;
import classfile.ConstantType;
import classfile.JavaMethod;
import classfile.MethodReference;
import classfile.Primitive;
import classfile.code.Code;
import classfile.code.opcodes.Arithmetic;
import classfile.code.opcodes.ArithmeticType;
import classfile.code.opcodes.ArrayInstantiation;
import classfile.code.opcodes.CompareJump;
import classfile.code.opcodes.Comparison;
import classfile.code.opcodes.ComputationalType;
import classfile.code.opcodes.ConditionalJump;
import classfile.code.opcodes.Conversion;
import classfile.code.opcodes.ExceptionHandler;
import classfile.code.opcodes.FieldAccessor;
import classfile.code.opcodes.JumpCondition;
import classfile.code.opcodes.LocalVariable;
import classfile.code.opcodes.LocalVariableIncrement;
import classfile.code.opcodes.MethodInvocation;
import classfile.code.opcodes.MethodType;
import classfile.code.opcodes.Opcode;
import classfile.code.opcodes.Shift;
import classfile.code.opcodes.StackManagement;
import classfile.code.opcodes.Switch;

public class MethodAnalyzer {

	private final JavaMethod method;
	private final Code code;

	private Map<BasicBlock, BasicBlock> basicBlockOrdering = new HashMap<>();
	private Map<BasicBlock, Set<Integer>> basicBlockSuccessors = new HashMap<>();
	private final NavigableMap<Integer, BasicBlock> basicBlocks;
	private final BasicBlock startBasicBlock;

	private Queue<BasicBlock> visitQueue = new LinkedList<>();
	private Map<BasicBlock, Map<DataBlock, Deque<Value>>> stackDefinitions = new HashMap<>();
	private Map<BasicBlock, Map<DataBlock, List<Value>>> localDefinitions = new HashMap<>();
	private Deque<Value> stack;
	private List<Value> locals;
	private int localsCount;
	private int dupCount = 0;

	private Map<BasicBlock, Map<DataConnection, BasicBlock>> dataConnections = new HashMap<>();
	private Map<BasicBlock, DataBlock> dataBlockMap = new HashMap<>();

	private final Set<DataBlock> dataBlocks;
	private final DataBlock startDataBlock;

	/*
	 * TODO:
	 * Handle back edges properly in the merging procedure
	 * Use StackMapTable instead of/in addition to inferring
	 * SUBROUTINES
	 * EXCEPTION HANDLERS
	 */

	public MethodAnalyzer(JavaMethod method) {
		this.method = method;
		code = method.code;
		basicBlocks = createBasicBlocks();
		startBasicBlock = basicBlocks.get(0);
		connectBasicBlock(startBasicBlock, new HashSet<BasicBlock>(), new HashSet<BasicBlock>());
		createDataBlocks();
		dataBlocks = connectDataBlocks();
		startDataBlock = dataBlockMap.get(startBasicBlock);
	}

	public String dataGML() {
		PrettyPrinter gml = new PrettyPrinter();
		gml.println("graph [")
		.indent()
		.println("directed 1")
		.println("node [")
		.indent()
		.println("id 0")
		.println("graphics [ fill \"#00FF00\" ]")
		.println("LabelGraphics [ text \"Start\" ]")
		.unindent()
		.println("]")
		.println("node [")
		.indent()
		.println("id 1")
		.println("graphics [ fill \"#FF0000\" ]")
		.println("LabelGraphics [ text \"End\" ]")
		.unindent()
		.println("]");
		for (DataBlock dataBlock : dataBlocks) {
			gml.println("node [")
			.indent()
			.print("id ").println(Integer.toString(dataBlock.hashCode()))
			.print("LabelGraphics [ text \"")
			.indent();
			String sep = "";
			for (Assignment assignment : dataBlock.assignments) {
				gml.print(sep).print(assignment.toString().replace("\"", "").replace("&", ""));
				sep = PrettyPrinter.NEWLINE;
			}
			gml.print(sep).print(dataBlock.transition.toString().replace("\"", "").replace("&", ""))
			.unindent()
			.println("\" ]")
			.unindent()
			.println("]");
		}
		for (DataBlock dataBlock : dataBlocks) {
			Map<DataConnection, DataBlock> successors = dataBlock.transition.successors;
			if (successors.isEmpty()) {
				gml.println("edge [")
				.indent()
				.print("source ").println(Integer.toString(dataBlock.hashCode()))
				.println("target 1")
				.unindent()
				.println("]");
			} else {
				for (Map.Entry<DataConnection, DataBlock> successor : successors.entrySet()) {
					gml.println("edge [")
					.indent()
					.print("source ").println(Integer.toString(dataBlock.hashCode()))
					.print("target ").println(Integer.toString(successor.getValue().hashCode()))
					.println("LabelGraphics [")
					.indent()
					.print("text \"").print(successor.getKey().toString()).println("\"")
					.println("outline \"#000000\"")
					.print("fill \"#CCCCCC\"")
					.unindent()
					.println("]")
					.unindent()
					.println("]");
				}
			}
		}
		gml.println("edge [")
		.indent()
		.println("source 0")
		.print("target ").println(Integer.toString(startDataBlock.hashCode()))
		.unindent()
		.println("]")
		.unindent()
		.println("]");
		return gml.toString();
	}

	private NavigableMap<Integer, BasicBlock> createBasicBlocks() {
		NavigableMap<Integer, BasicBlock> blockMap = new TreeMap<>();
		NavigableSet<Integer> leaders = new TreeSet<>();
		leaders.add(0);
		Map<Integer, Set<Integer>> successorMap = new HashMap<>();
		int codeSize = code.ops.size();
		for (int pc = 0; pc < codeSize; pc++) {
			Opcode op = code.ops.get(pc);
			if (op == null) {
				continue;
			}
			Set<Integer> successors = new HashSet<>();
			boolean addNext = false;
			switch (op.type) {
			case CONDITIONAL_JUMP:
			{
				int conditionalTarget = ((ConditionalJump) op.data).jumpTarget;
				leaders.add(conditionalTarget);
				successors.add(conditionalTarget);
				addNext = true;
				break;
			}
			case COMPARE_JUMP:
				int compareTarget = ((CompareJump) op.data).jumpTarget;
				leaders.add(compareTarget);
				successors.add(compareTarget);
				addNext = true;
				break;
			case UNCONDITIONAL_JUMP:
				int jumpTarget = (int) op.data;
				leaders.add(jumpTarget);
				successors.add(jumpTarget);
				break;
			case SUBROUTINE_JUMP:
				int subroutine = (int) op.data;
				leaders.add(subroutine);
				successors.add(subroutine);
				break;
			case RETURN:
				break;
			case SUBROUTINE_RETURN:
				break;
			case SWITCH:
				Switch switchOp = (Switch) op.data;
				for (int switchTarget : switchOp.jumpTable.values()) {
					leaders.add(switchTarget);
					successors.add(switchTarget);
				}
				int defaultTarget = switchOp.defaultJump;
				leaders.add(defaultTarget);
				successors.add(defaultTarget);
				break;
			case THROW:
				break;
			default:
				continue;
			}
			for (int i = pc + 1; i < codeSize; i++) {
				if (code.ops.get(i) != null) {
					leaders.add(i);
					if (addNext) {
						successors.add(i);
					}
					break;
				}
			}
			successorMap.put(pc, successors);
		}
		for (ExceptionHandler handler : code.exceptionTable) {
			leaders.add(handler.start);
			leaders.add(handler.end);
			leaders.add(handler.handler);
		}
		Map<BasicBlock, Integer> followers = new HashMap<>();

		List<Object> nullList = Collections.singletonList(null);
		int currentLeader = 0, nextLeader;
		for (Iterator<Integer> iter = leaders.iterator(); iter.hasNext(); currentLeader = nextLeader) {
			nextLeader = iter.next();
			List<Opcode> body = new ArrayList<>(code.ops.subList(currentLeader, nextLeader));
			body.removeAll(nullList);
			BasicBlock block = new BasicBlock(body);
			followers.put(block, nextLeader);
			blockMap.put(currentLeader, block);
		}
		List<Opcode> lastBody = new ArrayList<>(code.ops.subList(currentLeader, codeSize));
		lastBody.removeAll(nullList);
		blockMap.put(currentLeader, new BasicBlock(lastBody));

		for (Map.Entry<BasicBlock, Integer> entry : followers.entrySet()) {
			basicBlockOrdering.put(entry.getKey(), blockMap.get(entry.getValue()));
		}

		for (Map.Entry<Integer, Set<Integer>> entry : successorMap.entrySet()) {
			Map.Entry<Integer, BasicBlock> block = blockMap.floorEntry(entry.getKey());
			leaders.remove(block.getKey());
			basicBlockSuccessors.put(block.getValue(), entry.getValue());
		}
		for (int fallThrough : leaders) {
			Integer nextBlock = blockMap.higherKey(fallThrough);
			if (nextBlock != null) {
				basicBlockSuccessors.put(blockMap.get(fallThrough), Collections.singleton(nextBlock));
			}
		}
		return blockMap;
	}

	private void connectBasicBlock(BasicBlock block, Set<BasicBlock> exploring, Set<BasicBlock> visited) {
		if (!visited.contains(block)) {
			exploring.add(block);
			for (int successorPc : basicBlockSuccessors.get(block)) {
				BasicBlock successor = basicBlocks.get(successorPc);
				if (exploring.contains(successor)) {
					BasicBlock.connect(block, successor, true);
				} else {
					BasicBlock.connect(block, successor, false);
					connectBasicBlock(successor, exploring, visited);
				}
			}
			exploring.remove(block);
			visited.add(block);
		}
	}

	private void createDataBlocks() {
		for (BasicBlock block : basicBlocks.values()) {
			stackDefinitions.put(block, new HashMap<DataBlock, Deque<Value>>());
			localDefinitions.put(block, new HashMap<DataBlock, List<Value>>());
		}

		MethodReference methodReference = method.reference;
		Deque<Value> startStack = new LinkedList<>();
		stackDefinitions.get(startBasicBlock).put(null, startStack);
		localsCount = code.maxLocals;
		List<Value> arguments = new ArrayList<Value>(localsCount);
		if (!method.flags.isStatic) {
			arguments.add(new Value(ValueType.THIS, 0, methodReference.enclosingClass));
		}
		List<ClassReference> argumentTypes = methodReference.argTypes;
		int argumentCount = argumentTypes.size();
		for (int argumentIndex = 0; argumentIndex < argumentCount; argumentIndex++) {
			ClassReference argumentType = argumentTypes.get(argumentIndex);
			arguments.add(new Value(ValueType.ARGUMENT, argumentIndex, argumentType));
			if (argumentType.getComputationalType().category == 2) {
				arguments.add(null);
			}
		}
		for (int i = arguments.size(); i < localsCount; i++) {
			arguments.add(null);
		}
		localDefinitions.get(startBasicBlock).put(null, arguments);

		visitQueue.add(startBasicBlock);
		Set<BasicBlock> visited = new HashSet<>();
		while (!visitQueue.isEmpty()) {
			visitBlock(visitQueue.poll(), visited);
		}
	}

	private Set<DataBlock> connectDataBlocks() {
		for (Map.Entry<BasicBlock, Map<DataConnection, BasicBlock>> connectionEntry : dataConnections.entrySet()) {
			DataTransition transition = dataBlockMap.get(connectionEntry.getKey()).transition;
			Map<DataConnection, BasicBlock> connections = connectionEntry.getValue();
			for (Map.Entry<DataConnection, BasicBlock> successor : connections.entrySet()) {
				transition.successors.put(successor.getKey(), dataBlockMap.get(successor.getValue()));
			}
		}
		return new HashSet<>(dataBlockMap.values());
	}

	private void visitBlock(BasicBlock block, Set<BasicBlock> visited) {
		if (!visited.contains(block)) {
			visited.add(block);
			for (Map.Entry<BasicBlock, Boolean> predecessorEntry : block.backwardEdges.entrySet()) {
				BasicBlock predecessor = predecessorEntry.getKey();
				if (!predecessorEntry.getValue() && !visited.contains(predecessor)) {
					visitBlock(predecessor, visited);
				}
			}
			stack = new LinkedList<>(mergeStacks(stackDefinitions.get(block)));
			locals = new ArrayList<>(mergeLocals(localDefinitions.get(block)));
			DataBlock dataBlock = createDataBlock(block);
			dataBlockMap.put(block, dataBlock);
			for (Map.Entry<BasicBlock, Boolean> successorEntry : block.forwardEdges.entrySet()) {
				if (!successorEntry.getValue()) {
					BasicBlock successor = successorEntry.getKey();
					stackDefinitions.get(successor).put(dataBlock, stack);
					localDefinitions.get(successor).put(dataBlock, locals);
					visitQueue.add(successor);
				}
			}
		}
	}

	private DataBlock createDataBlock(BasicBlock block) {
		DataBlock dataBlock = new DataBlock();
		for (Opcode op : block.body) {
			switch (op.type) {
			case NOOP:
			{
				break;
			}
			case CONSTANT_LOAD:
			{
				ConstantEntry constant = (ConstantEntry) op.data;
				ClassReference constantClass = constant.type.classType;
				if (constantClass.getComputationalType().category == 2) {
					stack.push(null);
				}
				stack.push(new Value(ValueType.CONSTANT, constant, constantClass));
				break;
			}
			case LOCAL_LOAD:
			{
				LocalVariable local = (LocalVariable) op.data;
				int localIndex = local.index;
				if (local.type.category == 2) {
					stack.push(null);
				}
				stack.push(locals.get(localIndex));
				break;
			}
			case ARRAY_LOAD:
			{
				Value arrayIndex = stack.pop();
				Value arrayReference = stack.pop();
				ClassReference elementType = ClassReference.elementFromArrayType(arrayReference.classType);
				if (elementType.getComputationalType().category == 2) {
					stack.push(null);
				}
				stack.push(new Value(ValueType.ARRAY_ACCESS, new ArrayAccess(arrayReference, arrayIndex), elementType));
				break;
			}
			case LOCAL_STORE:
			{
				LocalVariable local = (LocalVariable) op.data;
				int localIndex = local.index;
				Value localValue = stack.pop();
				ClassReference localClass = localValue.classType;
				if (localClass.getComputationalType().category == 2) {
					stack.pop();
				}
				Value localTarget = new Value(ValueType.LOCAL, localIndex, localClass);
				locals.set(localIndex, localTarget);
				dataBlock.assignments.add(new Assignment(localTarget, localValue));
				break;
			}
			case ARRAY_STORE:
			{
				Value arrayValue = stack.pop();
				if (arrayValue.classType.getComputationalType().category == 2) {
					stack.pop();
				}
				Value arrayIndex = stack.pop();
				Value arrayReference = stack.pop();
				dataBlock.assignments.add(new Assignment(new Value(ValueType.ARRAY_ACCESS, new ArrayAccess(arrayReference, arrayIndex), ClassReference.elementFromArrayType(arrayReference.classType)), arrayValue));
				break;
			}
			case STACK_MANAGE:
			{
				switch ((StackManagement) op.data) {
				case POP:
				{
					dataBlock.assignments.add(new Assignment(Value.POP, stack.pop()));
					break;
				}
				case POP2:
				{
					dataBlock.assignments.add(new Assignment(Value.POP, stack.pop()));
					Value pop2 = stack.pop();
					if (pop2 != null) {
						dataBlock.assignments.add(new Assignment(Value.POP, pop2));
					}
					break;
				}
				case DUP:
				{
					Value dupValue = stack.pop();
					Value dup;
					if (isCacheSafe(dupValue)) {
						dup = dupValue;
					} else {
						dup = new Value(ValueType.DUP, dupCount++, dupValue.classType);
						dataBlock.assignments.add(new Assignment(dup, dupValue));
					}
					stack.push(dup);
					stack.push(dup);
					break;
				}
				case DUP_X1:
				{
					Value value1 = stack.pop();
					Value value2 = stack.pop();
					Value dup;
					if (isCacheSafe(value1)) {
						dup = value1;
					} else {
						dup = new Value(ValueType.DUP, dupCount++, value1.classType);
						dataBlock.assignments.add(new Assignment(dup, value1));
					}
					stack.push(dup);
					stack.push(value2);
					stack.push(dup);
					break;
				}
				case DUP_X2:
				{
					Value value1 = stack.pop();
					Value value2 = stack.pop();
					Value value3 = stack.pop();
					Value dup;
					if (isCacheSafe(value1)) {
						dup = value1;
					} else {
						dup = new Value(ValueType.DUP, dupCount++, value1.classType);
						dataBlock.assignments.add(new Assignment(dup, value1));
					}
					stack.push(dup);
					stack.push(value3);
					stack.push(value2);
					stack.push(dup);
					break;
				}
				case DUP2:
				{
					Value value1 = stack.pop();
					Value value2 = stack.pop();
					Value dup1;
					if (isCacheSafe(value1)) {
						dup1 = value1;
					} else {
						dup1 = new Value(ValueType.DUP, dupCount++, value1.classType);
						dataBlock.assignments.add(new Assignment(dup1, value1));
					}
					Value dup2;
					if (value2 != null) {
						if (isCacheSafe(value2)) {
							dup2 = value2;
						} else {
							dup2 = new Value(ValueType.DUP, dupCount++, value2.classType);
							dataBlock.assignments.add(new Assignment(dup1, value2));
						}
					} else {
						dup2 = null;
					}
					stack.push(dup2);
					stack.push(dup1);
					stack.push(dup2);
					stack.push(dup1);
					break;
				}
				case DUP2_X1:
				{
					Value value1 = stack.pop();
					Value value2 = stack.pop();
					Value value3 = stack.pop();
					Value dup1;
					if (isCacheSafe(value1)) {
						dup1 = value1;
					} else {
						dup1 = new Value(ValueType.DUP, dupCount++, value1.classType);
						dataBlock.assignments.add(new Assignment(dup1, value1));
					}
					Value dup2;
					if (value2 != null) {
						if (isCacheSafe(value2)) {
							dup2 = value2;
						} else {
							dup2 = new Value(ValueType.DUP, dupCount++, value2.classType);
							dataBlock.assignments.add(new Assignment(dup1, value2));
						}
					} else {
						dup2 = null;
					}
					stack.push(dup2);
					stack.push(dup1);
					stack.push(value3);
					stack.push(dup2);
					stack.push(dup1);
					break;
				}
				case DUP2_X2:
				{
					Value value1 = stack.pop();
					Value value2 = stack.pop();
					Value value3 = stack.pop();
					Value value4 = stack.pop();
					Value dup1;
					if (isCacheSafe(value1)) {
						dup1 = value1;
					} else {
						dup1 = new Value(ValueType.DUP, dupCount++, value1.classType);
						dataBlock.assignments.add(new Assignment(dup1, value1));
					}
					Value dup2;
					if (value2 != null) {
						if (isCacheSafe(value2)) {
							dup2 = value2;
						} else {
							dup2 = new Value(ValueType.DUP, dupCount++, value2.classType);
							dataBlock.assignments.add(new Assignment(dup1, value2));
						}
					} else {
						dup2 = null;
					}
					stack.push(dup2);
					stack.push(dup1);
					stack.push(value4);
					stack.push(value3);
					stack.push(dup2);
					stack.push(dup1);
					break;
				}
				case SWAP:
				{
					Value value1 = stack.pop();
					Value value2 = stack.pop();
					stack.push(value1);
					stack.push(value2);
					break;
				}
				default:
					break;
				}
				break;
			}
			case ARITHMETIC:
			{
				Arithmetic arithmetic = (Arithmetic) op.data;
				ComputationalType operatingType = arithmetic.operatingType;
				Value right = stack.pop();
				if (operatingType.category == 2) {
					stack.pop();
				}
				Value left = stack.pop();
				stack.push(new Value(ValueType.BINARY_ARITHMETIC, new BinaryArithmetic(left, right, arithmetic.operation), ClassReference.fromComputationalType(operatingType)));
				break;
			}
			case NEGATE:
			{
				ComputationalType negateType = (ComputationalType) op.data;
				stack.push(new Value(ValueType.NEGATE, stack.pop(), ClassReference.fromComputationalType(negateType)));
				break;
			}
			case SHIFT:
			{
				Shift shift = (Shift) op.data;
				Value shiftAmount = stack.pop();
				Value shifted = stack.pop();
				stack.push(new Value(ValueType.SHIFT, new ShiftArithmetic(shifted, shiftAmount, shift.shiftType), ClassReference.fromComputationalType(shift.operatingType)));
				break;
			}
			case LOCAL_INCREMENT:
			{
				LocalVariableIncrement localIncrement = (LocalVariableIncrement) op.data;
				LocalVariable local = localIncrement.local;
				Value localTarget = new Value(ValueType.LOCAL, local.index, ClassReference.fromComputationalType(ComputationalType.INT));
				dataBlock.assignments.add(new Assignment(localTarget, new Value(ValueType.BINARY_ARITHMETIC, new BinaryArithmetic(localTarget, new Value(ValueType.CONSTANT, new ConstantEntry(ConstantType.INTEGER, localIncrement.incrementAmount), ClassReference.fromComputationalType(ComputationalType.INT)), ArithmeticType.ADD), localTarget.classType)));
				break;
			}
			case CONVERT:
			{
				Conversion conversion = (Conversion) op.data;
				Value value = stack.pop();
				if (conversion.from.computationalType.category == 2) {
					stack.pop();
				}
				Primitive targetPrimitive = conversion.to;
				if (targetPrimitive.computationalType.category == 2) {
					stack.push(null);
				}
				stack.push(new Value(ValueType.CONVERT, new Convert(value, targetPrimitive), ClassReference.fromPrimitive(targetPrimitive)));
				break;
			}
			case COMPARE:
			{
				Comparison comparison = (Comparison) op.data;
				ComputationalType compareType = comparison.compareType;
				boolean category2 = (compareType.category == 2);
				Value value2 = stack.pop();
				if (category2) {
					stack.pop();
				}
				Value value1 = stack.pop();
				if (category2) {
					stack.pop();
				}
				stack.push(new Value(ValueType.COMPARE, new Compare(value1, value2, comparison.option), ClassReference.fromComputationalType(ComputationalType.INT)));
				break;
			}
			case CONDITIONAL_JUMP:
			{
				ConditionalJump conditionalJump = (ConditionalJump) op.data;
				BasicBlock ifBlock = basicBlocks.get((int) conditionalJump.jumpTarget);
				BasicBlock elseBlock = basicBlockOrdering.get(block);
				Value compareTo;
				JumpCondition condition = conditionalJump.condition;
				if (condition == JumpCondition.IS_NULL || condition == JumpCondition.IS_NOT_NULL) {
					compareTo = Value.NULL;
				} else {
					compareTo = Value.ZERO;
				}
				dataBlock.transition = DataTransition.fromComparison(new ValueComparison(stack.pop(), ComparisonType.fromCondition(conditionalJump.condition), compareTo));
				if (conditionalJump.conditionType.category == 2) {
					stack.pop();
				}
				Map<DataConnection, BasicBlock> connections = new HashMap<>();
				connections.put(DataConnection.IF, ifBlock);
				connections.put(DataConnection.ELSE, elseBlock);
				dataConnections.put(block, connections);
				break;
			}
			case COMPARE_JUMP:
			{
				CompareJump compareJump = (CompareJump) op.data;
				BasicBlock ifBlock = basicBlocks.get((int) compareJump.jumpTarget);
				BasicBlock elseBlock = basicBlockOrdering.get(block);
				boolean category2 = (compareJump.compareType.category == 2);
				Value left = stack.pop();
				if (category2) {
					stack.pop();
				}
				Value right = stack.pop();
				if (category2) {
					stack.pop();
				}
				dataBlock.transition = DataTransition.fromComparison(new ValueComparison(left, ComparisonType.fromCompare(compareJump.comparison), right));
				Map<DataConnection, BasicBlock> connections = new HashMap<>();
				connections.put(DataConnection.IF, ifBlock);
				connections.put(DataConnection.ELSE, elseBlock);
				dataConnections.put(block, connections);
				break;
			}
			case UNCONDITIONAL_JUMP:
			{
				dataBlock.transition = DataTransition.fromUnconditional();
				dataConnections.put(block, Collections.singletonMap(DataConnection.UNCONDITIONAL, basicBlocks.get((int) op.data)));
				break;
			}
			case SUBROUTINE_JUMP:
			{
				//System.err.println("I hate subroutines");
				stack.push(Value.NULL);
				dataBlock.transition = DataTransition.fromUnconditional();
				dataConnections.put(block, Collections.singletonMap(DataConnection.UNCONDITIONAL, basicBlocks.get((int) op.data)));
				break;
			}
			case SUBROUTINE_RETURN:
			{
				//System.err.println("I hate subroutines");
				dataBlock.transition = DataTransition.fromUnconditional();
				break;
			}
			case SWITCH:
			{
				Switch switchOp = (Switch) op.data;
				Value switchValue = stack.pop();
				dataBlock.transition = DataTransition.fromSwitch(switchValue);
				Map<DataConnection, BasicBlock> connections = new HashMap<>();
				for (Map.Entry<Integer, Integer> switchJump : switchOp.jumpTable.entrySet()) {
					connections.put(DataConnection.fromSwitch(switchJump.getKey()), basicBlocks.get(switchJump.getValue()));
				}
				connections.put(DataConnection.DEFAULT, basicBlocks.get(switchOp.defaultJump));
				dataConnections.put(block, connections);
				break;
			}
			case RETURN:
			{
				ClassReference returnClass = method.reference.returnType;
				if (returnClass.primitive != Primitive.VOID) {
					Value returnValue = stack.pop();
					dataBlock.transition = DataTransition.fromReturn(returnValue);
					if (returnClass.getComputationalType().category == 2) {
						stack.pop();
					}
				} else {
					dataBlock.transition = DataTransition.fromReturn(Value.VOID);
				}
				break;
			}
			case FIELD_LOAD:
			{
				FieldAccessor fieldAccess = (FieldAccessor) op.data;
				Value instance = (fieldAccess.isStatic ? null : stack.pop());
				ClassReference fieldType = fieldAccess.field.type;
				if (fieldType.getComputationalType().category == 2) {
					stack.push(null);
				}
				stack.push(new Value(ValueType.FIELD_ACCESS, new FieldAccess(instance, fieldAccess), fieldType));
				break;
			}
			case FIELD_STORE:
			{
				FieldAccessor fieldAccess = (FieldAccessor) op.data;
				Value fieldValue = stack.pop();
				if (fieldValue.classType.getComputationalType().category == 2) {
					stack.pop();
				}
				Value instance = (fieldAccess.isStatic ? null : stack.pop());
				dataBlock.assignments.add(new Assignment(new Value(ValueType.FIELD_ACCESS, new FieldAccess(instance, fieldAccess), fieldAccess.field.type), fieldValue));
				break;
			}
			case METHOD_INVOKE:
			{
				MethodInvocation methodCall = (MethodInvocation) op.data;
				MethodReference methodReference = methodCall.method;
				int argumentCount = methodReference.argTypes.size();
				List<Value> arguments = new ArrayList<>(argumentCount);
				for (int i = 0; i < argumentCount; i++) {
					Value argument = stack.pop();
					arguments.add(argument);
					if (argument.classType.getComputationalType().category == 2) {
						stack.pop();
					}
				}
				Collections.reverse(arguments);
				Value instance = (methodCall.type == MethodType.STATIC ? null : stack.pop());
				ClassReference returnType = methodReference.returnType;
				Value methodValue = new Value(ValueType.METHOD_CALL, new MethodCall(instance, arguments, methodCall), returnType);
				if (returnType.primitive == Primitive.VOID) {
					dataBlock.assignments.add(new Assignment(Value.VOID, methodValue));
				} else {
					if (returnType.getComputationalType().category == 2) {
						stack.push(null);
					}
					stack.push(methodValue);
				}
				break;
			}
			case NEW_OBJECT:
			{
				ClassReference instanceClass = (ClassReference) op.data;
				stack.push(new Value(ValueType.NEW_INSTANCE, instanceClass, instanceClass));
				break;
			}
			case NEW_ARRAY:
			{
				ArrayInstantiation instantiation = (ArrayInstantiation) op.data;
				int dimensionsCreated = instantiation.dimensionsCreated;
				List<Value> counts = new ArrayList<>(dimensionsCreated);
				for (int i = 0; i < dimensionsCreated; i++) {
					counts.add(stack.pop());
				}
				Collections.reverse(counts);
				ClassReference arrayClass = instantiation.arrayClass;
				stack.push(new Value(ValueType.NEW_ARRAY, new NewArray(arrayClass, counts), arrayClass));
				break;
			}
			case ARRAY_LENGTH:
			{
				stack.push(new Value(ValueType.ARRAY_LENGTH, stack.pop(), ClassReference.fromComputationalType(ComputationalType.INT)));
				break;
			}
			case THROW:
			{
				Value thrown = stack.pop();
				dataBlock.transition = DataTransition.fromThrow();
				dataBlock.assignments.add(new Assignment(new Value(ValueType.THROW, null, thrown.classType), thrown));
				break;
			}
			case CAST:
			{
				ClassReference castTo = (ClassReference) op.data;
				stack.push(new Value(ValueType.CAST, new Cast(stack.pop(), castTo), castTo));
				break;
			}
			case INSTANCE_OF:
			{
				ClassReference instanceOf = (ClassReference) op.data;
				stack.push(new Value(ValueType.INSTANCE_OF, new InstanceOf(stack.pop(), instanceOf), ClassReference.fromComputationalType(ComputationalType.INT)));
				break;
			}
			case SYNCHRONIZE:
			{
				if ((boolean) op.data) {
					dataBlock.assignments.add(new Assignment(Value.MONITOR, stack.pop()));
				} else {
					dataBlock.assignments.add(new Assignment(Value.RELEASE, stack.pop()));
				}
				break;
			}
			}
		}
		if (dataBlock.transition == null) {
			Set<BasicBlock> successors = block.forwardEdges.keySet();
			if (successors.size() == 1) {
				dataBlock.transition = DataTransition.fromFallThrough();
				dataConnections.put(block, Collections.singletonMap(DataConnection.FALL_THROUGH, successors.iterator().next()));
			}
		}
		return dataBlock;
	}

	private Deque<Value> mergeStacks(Map<DataBlock, Deque<Value>> stackMap) {
		int stackCount = stackMap.size();
		if (stackCount == 1) {
			return stackMap.values().iterator().next();
		} else {
			Deque<Value> merged = new LinkedList<>();
			Deque<Map<DataBlock, Value>> stackValues = new LinkedList<>();
			Deque<Set<ClassReference>> stackClasses = new LinkedList<>();
			int stackHeight = stackMap.values().iterator().next().size();
			for (int i = 0; i < stackHeight; i++) {
				stackValues.push(new HashMap<DataBlock, Value>());
				stackClasses.push(new HashSet<ClassReference>());
			}
			for (Map.Entry<DataBlock, Deque<Value>> stackEntry : stackMap.entrySet()) {
				for (Value stackValue : stackEntry.getValue()) {
					Map<DataBlock, Value> values = stackValues.remove();
					Set<ClassReference> classes = stackClasses.remove();
					if (stackValue != null) {
						values.put(stackEntry.getKey(), stackValue);
						classes.add(stackValue.classType);
					}
					stackClasses.add(classes);
					stackValues.add(values);
				}
			}
			int stackIndex = stackHeight - 1;
			for (Set<ClassReference> classes : stackClasses) {
				Map<DataBlock, Value> values = stackValues.pop();
				if (new HashSet<>(values.values()).size() == 1) {
					merged.add(values.values().iterator().next());
				} else {
					if (!classes.isEmpty()) {
						Value newStack = new Value(ValueType.STACK, stackIndex, ClassReference.leastCommonSuperclass(classes));
						merged.add(newStack);
						for (Map.Entry<DataBlock, Value> valueEntry : values.entrySet()) {
							Value value = valueEntry.getValue();
							valueEntry.getKey().assignments.add(new Assignment(new Value(ValueType.STACK, stackIndex, value.classType), value));
						}
					} else {
						merged.add(null);
					}
				}
				stackIndex--;
			}
			//System.out.println("Merged stack " + merged);
			return merged;
		}
	}

	private List<Value> mergeLocals(Map<DataBlock, List<Value>> localsMap) {
		int localSetsCount = localsMap.size();
		if (localSetsCount == 1) {
			return localsMap.values().iterator().next();
		} else {
			List<Value> merged = new ArrayList<>(localsCount);
			List<Map<DataBlock, Value>> localValues = new ArrayList<>(localsCount);
			List<Set<ClassReference>> localClasses = new ArrayList<>(localsCount);
			for (int i = 0; i < localsCount; i++) {
				merged.add(null);
				localValues.add(new HashMap<DataBlock, Value>());
				localClasses.add(new HashSet<ClassReference>());
			}
			for (Map.Entry<DataBlock, List<Value>> localsEntry : localsMap.entrySet()) {
				List<Value> values = localsEntry.getValue();
				for (int localIndex = 0; localIndex < localsCount; localIndex++) {
					Value localValue = values.get(localIndex);
					Set<ClassReference> classes = localClasses.get(localIndex);
					if (localValue != null) {
						localValues.get(localIndex).put(localsEntry.getKey(), localValue);
						ClassReference localClass = localValue.classType;
						classes.add(localClass);
						if (localClass.getComputationalType().category == 2) {
							localIndex++;
						}
					}
				}
			}
			for (int localIndex = 0; localIndex < localsCount; localIndex++) {
				Set<ClassReference> classes = localClasses.get(localIndex);
				Map<DataBlock, Value> values = localValues.get(localIndex);
				if (new HashSet<>(values.values()).size() == 1) {
					Value localValue = values.values().iterator().next();
					merged.set(localIndex, localValue);
					if (localValue.classType.getComputationalType().category == 2) {
						localIndex++;
					}
				} else {
					if (!values.isEmpty()) {
						ClassReference localClass = ClassReference.leastCommonSuperclass(classes);
						if (localClass != null) {
							merged.set(localIndex, new Value(ValueType.LOCAL, localIndex, localClass));
							if (localClass.getComputationalType().category == 2) {
								localIndex++;
							}
						}
					}
				}
			}
			//System.out.println("Merged locals " + Arrays.toString(merged));
			return merged;
		}
	}

	private boolean isCacheSafe(Value value) {
		switch (value.type) {
		case CONSTANT:
		case THIS:
		case ARGUMENT:
		case LOCAL:
		case STACK:
			return true;
		case ARRAY_ACCESS:
			return false;
		case BINARY_ARITHMETIC:
			BinaryArithmetic arithmetic = (BinaryArithmetic) value.value;
			return isCacheSafe(arithmetic.left) && isCacheSafe(arithmetic.right);
		case NEGATE:
			return isCacheSafe((Value) value.value);
		case SHIFT:
			ShiftArithmetic shift = (ShiftArithmetic) value.value;
			return isCacheSafe(shift.shifted) && isCacheSafe(shift.shiftAmount);
		case CONVERT:
			Convert convert = (Convert) value.value;
			return isCacheSafe(convert.value);
		case COMPARE:
			Compare compare = (Compare) value.value;
			return isCacheSafe(compare.value1) && isCacheSafe(compare.value2);
		case FIELD_ACCESS:
		case METHOD_CALL:
		case VOID:
		case NEW_INSTANCE:
		case NEW_ARRAY:
			return false;
		case ARRAY_LENGTH:
			return isCacheSafe((Value) value.value);
		case THROW:
			return false;
		case CAST:
			Cast cast = (Cast) value.value;
			return isCacheSafe(cast.value);
		case INSTANCE_OF:
			InstanceOf instanceOf = (InstanceOf) value.value;
			return isCacheSafe(instanceOf.value);
		case MONITOR:
		case RELEASE:
		case POP:
			return false;
		case DUP:
			return true;
		default:
			return false;
		}
	}

}
