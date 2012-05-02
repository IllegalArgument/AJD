package classfile;

public class FieldReference {
	
	public final ClassReference enclosingClass;
	public final String name;
	public final ClassReference type;
	
	public FieldReference(ClassReference enclosingClass, NameAndType nat) {
		this(enclosingClass, nat.name, nat.type);
	}
	
	public FieldReference(ClassReference enclosingClass, String name, String type) {
		this.enclosingClass = enclosingClass;
		this.name = name;
		this.type = new ClassReference(type);
	}
	
	@Override
	public String toString() {
		return enclosingClass + "." + name + ":" + type;
	}

}
