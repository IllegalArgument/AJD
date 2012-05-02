package classfile;

class NameAndType {
	
	public final String name, type;

	public NameAndType(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	@Override
	public String toString() {
		return name + ": " + type;
	}

}
