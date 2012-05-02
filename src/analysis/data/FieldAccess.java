package analysis.data;

import classfile.code.FieldAccessor;

public class FieldAccess {
	
	public final Value instance;
	public final FieldAccessor accessor;
	
	public FieldAccess(Value instance, FieldAccessor accessor) {
		this.instance = instance;
		this.accessor = accessor;
	}
	
	@Override
	public String toString() {
		if (instance == null) {
			return accessor.toString();
		} else {
			return "(" + accessor.field.enclosingClass + ") " + instance + "." + accessor.field.name;
		}
	}

}
