package classfile;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import util.BufferUtils;
import util.PrettyPrinter;
import util.Printable;
import classfile.struct.ClassStruct;
import classfile.struct.ConstantEntryStruct;

public class JavaClass implements Printable {
	
	public static final int MAGIC = 0xCAFEBABE;

	public final ClassVersion version;
	private final ConstantEntry[] constantPool;
	public final ClassFlags flags;
	public final ClassReference thisType;
	public final ClassReference superType;
	public final List<ClassReference> interfaces;
	public final Map<FieldReference, JavaField> fields;
	public final Map<MethodReference, JavaMethod> methods;

	public JavaClass(ClassStruct struct) {
		if (struct.magic != MAGIC) {
			throw new ClassFormatException("Bad magic!");
		}
		version = new ClassVersion(struct.majorVersion, struct.minorVersion);
		constantPool = new ConstantEntry[struct.constantPoolCount];
		constantPool[0] = new ConstantEntry(ConstantType.NULL, null);
		for (int i = 1; i < struct.constantPoolCount; i++) {
			ConstantEntry entry = createConstantEntry(i, struct.constantPool);
			constantPool[i] = entry;
			if (entry.type == ConstantType.LONG || entry.type == ConstantType.DOUBLE) {
				i++;
			}
		}
		flags = new ClassFlags(struct.accessFlags);
		thisType = (ClassReference) constantPool[struct.thisClass].data;
		superType = (ClassReference) constantPool[struct.superClass].data;
		List<ClassReference> interfaces = new LinkedList<>();
		for (int i = 0; i < struct.interfacesCount; i++) {
			interfaces.add((ClassReference) constantPool[struct.interfaces[i]].data);
		}
		this.interfaces = Collections.unmodifiableList(interfaces);
		Map<FieldReference, JavaField> fields = new LinkedHashMap<>();
		for (int i = 0; i < struct.fieldsCount; i++) {
			JavaField field = new JavaField(this, struct.fields[i]);
			fields.put(field.reference, field);
		}
		this.fields = Collections.unmodifiableMap(fields);
		Map<MethodReference, JavaMethod> methods = new LinkedHashMap<>();
		for (int i = 0; i < struct.methodsCount; i++) {
			JavaMethod method = new JavaMethod(this, struct.methods[i]);
			methods.put(method.reference, method);
		}
		this.methods = Collections.unmodifiableMap(methods);
	}

	private ConstantEntry createConstantEntry(int index, ConstantEntryStruct[] pool) {
		if (constantPool[index] == null) {
			ConstantEntryStruct struct = pool[index];
			ByteBuffer info = struct.info;
			switch (struct.tag) {
			case ConstantEntryStruct.CLASS:
				constantPool[index] = new ConstantEntry(ConstantType.CLASS,
						ClassReference.fromConstant((String) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.FIELD_REF:
				constantPool[index] = new ConstantEntry(ConstantType.FIELD_REF,
						new FieldReference((ClassReference) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data,
								(NameAndType) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.METHOD_REF:
				constantPool[index] = new ConstantEntry(ConstantType.METHOD_REF,
						new MethodReference((ClassReference) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data,
								(NameAndType) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.INTERFACE_METHOD_REF:
				constantPool[index] = new ConstantEntry(ConstantType.INTERFACE_METHOD_REF,
						new MethodReference((ClassReference) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data,
								(NameAndType) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.STRING:
				constantPool[index] = new ConstantEntry(ConstantType.STRING,
						(String) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data);
				break;
			case ConstantEntryStruct.INTEGER:
				constantPool[index] = new ConstantEntry(ConstantType.INTEGER, info.getInt());
				break;
			case ConstantEntryStruct.FLOAT:
				constantPool[index] = new ConstantEntry(ConstantType.FLOAT, info.getFloat());
				break;
			case ConstantEntryStruct.LONG:
				constantPool[index] = new ConstantEntry(ConstantType.LONG, info.getLong());
				break;
			case ConstantEntryStruct.DOUBLE:
				constantPool[index] = new ConstantEntry(ConstantType.DOUBLE, info.getDouble());
				break;
			case ConstantEntryStruct.NAME_AND_TYPE:
				constantPool[index] = new ConstantEntry(ConstantType.NAME_AND_TYPE,
						new NameAndType((String) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data,
								(String) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.UTF8:
				constantPool[index] = new ConstantEntry(ConstantType.UTF8, BufferUtils.getModifiedUTF8(info));
				break;
			}
		}
		return constantPool[index];
	}
	
	public ConstantEntry getConstant(int index) {
		return constantPool[index];
	}
	
	@Override
	public void printOn(PrettyPrinter p) {
		p.print("class " + thisType + " extends " + superType);
		if (interfaces.size() > 0) {
			p.print(" implements ");
			String sep = "";
			for (ClassReference inter : interfaces) {
				p.print(sep + inter);
				sep = ", ";
			}
		}
		p.println(" [")
		.indent()
		.println("Version: " + version)
		/*.println("Constant Pool [")
		.indent();
		for (int i = 0; i < constantPool.length; i++) {
			p.println(i + ": " + constantPool[i]);
		}
		p.unindent()
		.println("]")*/
		.println("Flags: " + flags)
		.println("Fields [")
		.indent();
		for (Map.Entry<FieldReference, JavaField> field : fields.entrySet()) {
			p.print(field.getValue());
		}
		p.unindent()
		.println("]")
		.println("Methods [")
		.indent();
		for (Map.Entry<MethodReference, JavaMethod> method : methods.entrySet()) {
			p.print(method.getValue());
		}
		p.unindent()
		.println("]")
		.unindent()
		.println("]");
	}
	
}
