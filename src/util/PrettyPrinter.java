package util;

public class PrettyPrinter {
	
	public static final String NEWLINE = System.getProperty("line.separator");
	
	private final StringBuilder result;
	private int indentLevel = 0;
	private boolean isLineStart = true;
	
	public PrettyPrinter() {
		this.result = new StringBuilder();
	}
	
	public PrettyPrinter println(String s) {
		print(s);
		result.append(System.getProperty("line.separator"));
		isLineStart = true;
		return this;
	}
	
	public PrettyPrinter print(String s) {
		if (isLineStart) {
			for (int i = 0; i < indentLevel; i++) {
				result.append('\t');
			}
			isLineStart = false;
		}
		result.append(s);
		return this;
	}
	
	public PrettyPrinter print(Printable p) {
		p.printOn(this);
		return this;
	}
	
	public PrettyPrinter indent() {
		indentLevel++;
		return this;
	}
	
	public PrettyPrinter unindent() {
		if (indentLevel > 0) {
			indentLevel--;
		}
		return this;
	}
	
	@Override
	public String toString() {
		return result.toString();
	}

}
