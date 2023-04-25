import java.util.*;

public class LConstraint extends HashSet<Type.Label> {
	public LConstraint() { super(); }
	public LConstraint(LConstraint set) { super(set); }
	public LConstraint with(Type.Label label) {
		LConstraint set = new LConstraint(this);
		set.add(label);
		return set;
	}
	public String toString() {
		String s = "";
		String delim = "";
		for (Type.Label label : this) {
			s += delim + label;
			delim = ",";
		}
		return s;
	}
}
