package classfile;

public class ClassVersion {
	
	public final int major, minor;

	public ClassVersion(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}
	
	@Override
	public String toString() {
		return major + "." + minor;
	}
	
}
