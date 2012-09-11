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
import classfile.constant.ConstantEntry;
import classfile.constant.ConstantType;
import classfile.constant.NameAndType;
import classfile.struct.ClassStruct;
import classfile.struct.ConstantEntryStruct;

/**
 * An object containing all of the data of a Java class file, translated into a more manageable form. For details on the class file specification, please refer to the Java Virtual Machine Specification, version 2 or 3. A <code>JavaClass</code> only contains references to anything used in the class, but not a part of the class file; these references must be resolved in order to obtain the details of their associated objects.
 * 
 * @author Aaron Willey
 * @version 0.1
 */
public class JavaClass implements Printable {
	
	/**
	 * The magic number that begins all class files.
	 */
	public static final int MAGIC = 0xCAFEBABE;

	//WARNING! I do not believe much in getters or setters, especially for final fields of immutable instances
	
	/**
	 * The internal version of this class file.
	 */
	public final ClassVersion version;
	/**
	 * The constant pool of this class, which contains all of the constants used in the rest of the class file description.
	 */
	private ConstantEntry[] constantPool;
	/**
	 * The flags of this class, e.g., IS_INTERFACE, IS_ABSTRACT, etc.
	 */
	public final ClassFlags flags;
	/**
	 * The reference to the class of this class.
	 */
	public final ClassReference thisType;
	/**
	 * The reference to the superclass of this class.
	 */
	public final ClassReference superType;
	/**
	 * The interface(s) implemented by this class.
	 */
	public final List<ClassReference> interfaces;
	/**
	 * A mapping of references to each field in this class to its respective field object.
	 */
	public final Map<FieldReference, JavaField> fields;
	/**
	 * A mapping of references to each method in this class to its respective method object.
	 */
	public final Map<MethodReference, JavaMethod> methods;

	/**
	 * @param struct the raw structure of the class file to be used as a base for this <code>JavaClass</code>
	 */
	public JavaClass(ClassStruct struct) {
		//check the magic
		if (struct.magic != MAGIC) {
			throw new ClassFormatException("Bad magic!");
		}
		//create our nice version object
		version = new ClassVersion(struct.majorVersion, struct.minorVersion);
		//initialize the constant pool array
		constantPool = new ConstantEntry[struct.constantPoolCount];
		//for whatever reason, the constant pool entry at index 0 is implicitly null, so make sure we remember that
		constantPool[0] = ConstantEntry.NULL;
		//this also means that real constant pool entries start at index 1
		for (int i = 1; i < struct.constantPoolCount; i++) {
			//this both creates and stores the constant entry at this index, as well as any entries that the current one relies on
			ConstantEntry entry = createConstantEntry(i, struct.constantPool);
			//I hate this part, as it has caused too many bugs due to its omission
			//if a constant entry is a "big" entry, i.e., long or double, it uses up the constant entry index above the current one
			//you can't even address the higher index to access the constant entry, and even the JVMS says it was a stupid idea in retrospect, but... such is life
			if (entry.type == ConstantType.LONG || entry.type == ConstantType.DOUBLE) {
				i++;
			}
		}
		//surprise, flags are flags
		flags = new ClassFlags(struct.accessFlags);
		//create references to the type of this class, its superclass, and its implemented interfaces
		thisType = (ClassReference) constantPool[struct.thisClass].data;
		superType = (ClassReference) constantPool[struct.superClass].data;
		List<ClassReference> interfaces = new LinkedList<>();
		for (int i = 0; i < struct.interfacesCount; i++) {
			interfaces.add((ClassReference) constantPool[struct.interfaces[i]].data);
		}
		//see, it's unmodifiable, no need to worry about the lack of a getter or setter
		this.interfaces = Collections.unmodifiableList(interfaces);
		//similarly, we create fields for each field in the struct
		Map<FieldReference, JavaField> fields = new LinkedHashMap<>();
		for (int i = 0; i < struct.fieldsCount; i++) {
			JavaField field = new JavaField(this, struct.fields[i]);
			fields.put(field.reference, field);
		}
		this.fields = Collections.unmodifiableMap(fields);
		//...and methods for each method struct
		Map<MethodReference, JavaMethod> methods = new LinkedHashMap<>();
		for (int i = 0; i < struct.methodsCount; i++) {
			JavaMethod method = new JavaMethod(this, struct.methods[i]);
			methods.put(method.reference, method);
		}
		this.methods = Collections.unmodifiableMap(methods);
		//TODO: Finish up and handle the attribute structs that apply to the whole class
	}

	/**
	 * Creates a constant entry from given index in the struct pool, and stores it in the pool of this class. This method is called recursively in order to resolve any dependencies of one constant entry on another, e.g., a CONSTANT_STRING pointing to a CONSTANT_UTF8 entry resolving to a single <code>String</code> in a <code>ConstantEntry</code> object.
	 * 
	 * @param index the index of the constant entry to create
	 * @param pool the constant pool in its raw struct form
	 * @return the new constant entry or a preexisting entry, depending on whether the entry at <code>index</code> already exists
	 */
	private ConstantEntry createConstantEntry(int index, ConstantEntryStruct[] pool) {
		//only make the constant entry if one doesn't already exist
		if (constantPool[index] == null) {
			ConstantEntryStruct struct = pool[index];
			ByteBuffer info = struct.info;
			switch (struct.tag) {
			case ConstantEntryStruct.CLASS:
				//class entries take a string from a UTF8 entry to form the descriptor of the class
				constantPool[index] = new ConstantEntry(ConstantType.CLASS,
						ClassReference.fromConstant((String) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.FIELD_REF:
				//field entries depend on both a class entry and a name and type entry, for the class containing the field and the name and type of the field, respectively
				constantPool[index] = new ConstantEntry(ConstantType.FIELD_REF,
						new FieldReference((ClassReference) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data,
								(NameAndType) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.METHOD_REF:
				//similarly, method entries need a class entry for the enclosing class, and a name and type to for the method name and its signature
				constantPool[index] = new ConstantEntry(ConstantType.METHOD_REF,
						new MethodReference((ClassReference) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data,
								(NameAndType) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.INTERFACE_METHOD_REF:
				//as far as I can tell, interface method entries are effectively the same as regular method entries
				constantPool[index] = new ConstantEntry(ConstantType.INTERFACE_METHOD_REF,
						new MethodReference((ClassReference) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data,
								(NameAndType) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.STRING:
				//string entries depend on a UTF8 entry for their string data
				constantPool[index] = new ConstantEntry(ConstantType.STRING,
						(String) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data);
				break;
			case ConstantEntryStruct.INTEGER:
				//integer entries just store a single int read directly from the struct
				constantPool[index] = new ConstantEntry(ConstantType.INTEGER, info.getInt());
				break;
			case ConstantEntryStruct.FLOAT:
				//float entries read a float from the struct
				constantPool[index] = new ConstantEntry(ConstantType.FLOAT, info.getFloat());
				break;
			case ConstantEntryStruct.LONG:
				//long entries read a long
				constantPool[index] = new ConstantEntry(ConstantType.LONG, info.getLong());
				break;
			case ConstantEntryStruct.DOUBLE:
				//and double entries read a double
				constantPool[index] = new ConstantEntry(ConstantType.DOUBLE, info.getDouble());
				break;
			case ConstantEntryStruct.NAME_AND_TYPE:
				//a name and type entry contains a name from a UTF8 entry and a type (field type or method signature) from another UTF8 entry
				constantPool[index] = new ConstantEntry(ConstantType.NAME_AND_TYPE,
						new NameAndType((String) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data,
								(String) createConstantEntry(BufferUtils.getUnsignedShort(info), pool).data));
				break;
			case ConstantEntryStruct.UTF8:
				//the famous UTF8 entry contains raw data in the format of an almost, but not quite, UTF8 encoding 
				constantPool[index] = new ConstantEntry(ConstantType.UTF8, BufferUtils.getModifiedUTF8(info));
				break;
			}
		}
		return constantPool[index];
	}
	
	/**
	 * Gets the constant entry at a specified index in the constant pool.
	 * 
	 * @param index the index of the constant entry to access
	 * @return the constant entry at <code>index</code>
	 */
	public ConstantEntry getConstant(int index) {
		//see, now here, because constantPool is not immutable, I only allow access to it via this accessor method, but since there's now setting method, it is effectively immutable to all other classes
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
