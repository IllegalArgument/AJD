package classfile.code.stackmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FullFrame {
	
	public final List<VerificationItem> locals;
	public final List<VerificationItem> stack;
	
	public FullFrame(List<VerificationItem> locals, List<VerificationItem> stack) {
		this.locals = Collections.unmodifiableList(new ArrayList<>(locals));
		this.stack = Collections.unmodifiableList(new ArrayList<>(stack));
	}

	@Override
	public String toString() {
		return "Locals: " + locals + "; Stack: " + stack;
	}

}
