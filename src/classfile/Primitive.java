package classfile;

import classfile.code.opcodes.ComputationalType;

public enum Primitive {
	
	BOOLEAN("boolean", ComputationalType.INT),
	BYTE("byte", ComputationalType.INT),
	CHAR("char", ComputationalType.INT),
	SHORT("short", ComputationalType.INT),
	INT("int", ComputationalType.INT),
	LONG("long", ComputationalType.LONG),
	FLOAT("float", ComputationalType.FLOAT),
	DOUBLE("double", ComputationalType.DOUBLE),
	VOID("void", ComputationalType.VOID),
	REFERENCE("reference", ComputationalType.REFERENCE);
	
	public final String name;
	public final ComputationalType computationalType;
	
	private Primitive(String name, ComputationalType computationalType) {
		this.name = name;
		this.computationalType = computationalType;
	}

}
