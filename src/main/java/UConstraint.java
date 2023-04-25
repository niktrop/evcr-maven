import java.util.*;

public class UConstraint implements Comparable<UConstraint> {
	public final Type T1, T2;
	private int complexity;
	public UConstraint(Type T1, Type T2) {
		if (!(T1 instanceof Type.EvarApplication) || !(T2 instanceof Type.EvarApplication)
				|| ((Type.EvarApplication)T1).e != ((Type.EvarApplication)T2).e) {
			this.T1 = T1.elimOmega(); this.T2 = T2.elimOmega();
		}
		else { // Preserve the evar so that opusE() works
			this.T1 = T1; this.T2 = T2;
		}
		complexity = complexity();
	}
	public UConstraint(Expansion.Variable e, UConstraint constraint) {
		this(new Type.EvarApplication(e, constraint.T1),
				new Type.EvarApplication(e, constraint.T2));
	}
	public boolean solved() { return T1.equals(T2); }
	public UConstraint wrapEvar(Expansion.Variable e) {
		return new UConstraint(new Type.EvarApplication(e, T1), new Type.EvarApplication(e, T2));
	}
	public String toString() { return "(* "+complexity+" *) " + T1 + " == " + T2; }
	private int complexity() { return complexityOfKind() * complexityOfSize(); }
	private int complexityOfSize() { return T1.size() * T2.size(); }
	private int complexityOfKind() {
		Type _T1 = estrip(T1), _T2 = estrip(T2);
		if (T1.equals(T2)) return 0;
		else if (T1.isOmega() || T2.isOmega()) return 1;
		else if (solid(T1) || solid(T2)) return 2;
		else if (_T1 instanceof Type.SimpleVariable || _T2 instanceof Type.SimpleVariable)
			return 3;
		else if (solid(_T1) || solid(_T2)) return 4;
		else if (_T1 instanceof Type.SimpleVariable || _T2 instanceof Type.SimpleVariable)
			return 5;
		else if (!(_T1 instanceof Type.Intersection || _T2 instanceof Type.Intersection)) return 6;
		else return 7;
	}
	public static Type estrip(Type T) {
		if (T instanceof Type.EvarApplication) return estrip(((Type.EvarApplication)T).K);
		else return T;
	}
	private boolean solid(Type T)
	{ return T instanceof Type.Constant || T instanceof Type.Function; }
	public int compareTo(UConstraint other) {
		if (complexity < other.complexity) return -1;
		else if (complexity > other.complexity) return 1;
		else return toString().compareTo(other.toString());
	}
}
