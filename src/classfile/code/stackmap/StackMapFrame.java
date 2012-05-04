package classfile.code.stackmap;

public class StackMapFrame {
	
	public static final StackMapFrame
	SAME = new StackMapFrame(FrameType.SAME, null),
	SAME_EXTENDED = new StackMapFrame(FrameType.SAME_EXTENDED, null);
	
	public final FrameType type;
	public final Object frame;

	public StackMapFrame(FrameType type, Object frame) {
		this.type = type;
		this.frame = frame;
	}
	
	@Override
	public String toString() {
		return type + (frame != null ? " [" + frame + "]" : "");
	}

}
