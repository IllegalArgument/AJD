package classfile.constant;

/**
 * A <code>ConstantEntry</code> is the representation of one entry in the constant pool of the class file. However, unlike the struct representation of an entry, this class resolves all links within the constant pool, such that each entry has no dependence on any other entry.
 * 
 * @author Aaron Willey
 * @version 0.1
 */
public class ConstantEntry {
	
	/**
	 * The representation of the nonexistent constant entry at index 0.
	 */
	public static ConstantEntry NULL = new ConstantEntry(ConstantType.NULL, null);
	
	//WARNING! I find this style of having what is essentially a tagged object much easier than subclassing
	//sure, I could have this class be abstract, have a bunch of subclasses for each constant type, and then case based on the enum tag, which could *possibly* be a bit prettier, but it doesn't seem any more usable to me
	//I also forego the idea of getters and setters here - both fields are final and immutable, so it doesn't matter, in my opinion
	
	/**
	 * The type of constant entry this object represents.
	 */
	public final ConstantType type;
	/**
	 * The data associated with this constant entry, e.g., string for CONSTANT_STRING entries. This field will usually be cast based on the {@link ConstantEntry#type} of this object.
	 */
	public final Object data;
	
	/**
	 * Creates a new constant entry with the specified type and appropriate data.
	 * 
	 * @param type the type of the new constant entry
	 * @param data the associated data of the new constant entry
	 */
	public ConstantEntry(ConstantType type, Object data) {
		this.type = type;
		this.data = data;
	}
	
	@Override
	public String toString() {
		return type + (data != null ? " [" + data + "]" : "");
	}

}
