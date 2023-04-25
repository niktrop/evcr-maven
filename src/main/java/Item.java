import java.util.*;

public interface Item<X extends Item<X>> {
	// Factory methods
	X intersection(X item1, X item2);
	X evarApplication(Expansion.Variable e, X K);
	X omega();
	X elimOmega();

	X applySubstitution(Substitution s);

	public interface Variable {}
	public abstract class Abstract<X extends Item<X>> implements Item<X> {
		public boolean equals(Object other) { return toString().equals(other.toString()); }
		public int hashCode() { return toString().hashCode(); }
	}
	public abstract class Intersection<X extends Item<X>> extends Abstract<X> {
		protected X left, right;
		public Intersection(X left, X right) { this.left = left; this.right = right; }
		public X getLeft() { return left; }
		public X getRight() { return right; }
		public X applySubstitution(Substitution s)
		{ return intersection(s.apply(left), s.apply(right)); }
		public X elimOmega() {
			X left = this.left.elimOmega();
			X right = this.right.elimOmega();
			if (left instanceof Omega) return right;
			else if (right instanceof Omega) return left;
			else return intersection(left, right);
		}
	}
	public abstract class EvarApplication<X extends Item<X>> extends Abstract<X> {
		public Expansion.Variable e;
		public X K;
		public EvarApplication(Expansion.Variable e, X K) { this.e = e; this.K = K; }
		public X applySubstitution(Substitution s) { return s.apply(e).apply(K); }
		public X elimOmega() {
			X K = this.K.elimOmega();
			if (K instanceof Omega) return K;
			else return evarApplication(e, K);
		}
	}
	public abstract class Omega<X extends Item<X>> extends Abstract<X> {
		public String toString() { return "w"; }
		public X applySubstitution(Substitution s) { return omega(); }
		public X elimOmega() { return omega(); }
	}
}
