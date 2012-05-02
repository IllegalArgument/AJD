package analysis.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import classfile.ClassReference;

public class NewArray {

	public final ClassReference arrayClass;
	public final List<Value> counts;
	
	public NewArray(ClassReference arrayClass, List<Value> counts) {
		this.arrayClass = arrayClass;
		this.counts = Collections.unmodifiableList(new ArrayList<>(counts));
	}
	
	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(arrayClass.className);
		for (int i = 0; i < arrayClass.arrayDimension + 1; i++) {
			if (i < counts.size()) {
				result.append("[").append(counts.get(i)).append("]");
			} else {
				result.append("[]");
			}
		}
		return result.toString();
	}

}
