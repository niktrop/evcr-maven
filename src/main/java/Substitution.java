import java.util.*;

public class Substitution extends Expansion.Abstract {
	private Map<Item.Variable, Item> map = new HashMap<Item.Variable, Item>();
	public static final Substitution id = new Substitution();
	private Set<LConstraint> constraints = new HashSet<LConstraint>();
	public Substitution() {}
	public Substitution(Substitution s) { map.putAll(s.map); }
	public static Substitution orenaming(Set<Item.Variable> ovars) {
		Substitution s = new Substitution();
		for (Item.Variable X : ovars) {
			if (X instanceof Variable)
				s.put(X, new EvarApplication(
					new Variable(), new Substitution()
					));
			else if (X instanceof Type.SimpleVariable)
				s.put(X, new Type.SimpleVariable(((Type.SimpleVariable)X).constraint));
		}
		return s;
	}
	public void put(Item.Variable X, Item K) {
		if (X instanceof Type.SimpleVariable) {
			LConstraint constraint = ((Type.SimpleVariable)X).constraint;
			constraints.add(constraint);
			Type.Simple T = (Type.Simple)K;
			if (!T.meets(constraint))
				throw new RuntimeException(T + " does not meet constraint "+constraint);
		}
		else if (X instanceof Variable) {
			Expansion E = (Expansion)K;
		}
		map.put(X, K);
	}
	public Substitution elimOmega() {
		Substitution s = new Substitution();
		for (Item.Variable ix : keySet())
			s.put(ix, get(ix).elimOmega());
		return s;
	}
	public Substitution apply(Substitution s)
	{ return (Substitution)apply((Expansion)s); }
	public Type apply(Type item)
	{ return item.applySubstitution(this).elimOmega(); }
	public <X extends Item<X>> X apply(X item)
	{ return item.applySubstitution(this); }
	public UConstraint apply(UConstraint constraint)
	{ return new UConstraint(apply(constraint.T1), apply(constraint.T2)); }
	public UConstraintSet apply(UConstraintSet cset) {
		UConstraintSet result = new UConstraintSet();
		for (UConstraint constraint : cset)
			result.add(apply(constraint));
		return result;
	}
	public TermContext apply(TermContext termContext)
	{ return termContext.applySubstitution(this); }
	public Typing apply(Typing typing)
	{ return typing.applySubstitution(this).elimOmega(); }
	public Expansion apply(Variable e) {
		Expansion E = (Expansion)get(e);
		return E != null ? E : new EvarApplication(e, new Substitution());
	}
	public VarStruct apply(VarStruct varStruct)
	{ return varStruct.applySubstitution(this); }
	public Substitution applySubstitution(Substitution s) {
		Substitution s1 = new Substitution(s);
		for (Item.Variable ix : keySet())
			s1.put(ix, s.apply(get(ix)));
		return s1;
	}
	public Item get(Item.Variable X) { return map.get(X); }
	public Item remove(Item.Variable ix) { return map.remove(ix); }
	public Set<Item.Variable> keySet() { return map.keySet(); }
	public Substitution with(Item.Variable ix, Item i) {
		Substitution s = new Substitution(this);
		s.put(ix,i);
		return s;
	}
	public Substitution without(Item.Variable ix) {
		Substitution s = new Substitution(this);
		s.remove(ix);
		return s;
	}
	public Substitution restrictedTo(VarStruct vs) {
		if (vs == null)
			return new Substitution();
		Substitution s = new Substitution();
		for (Item.Variable X : map.keySet()) {
			if (vs.containsKey(X)) {
				Item K = map.get(X);
				if (K instanceof Type)
					s.put(X, K);
				else {
					Variable e = (Variable)X;
					Expansion E = (Expansion)K;
					s.put(e, E.restrictedTo(vs.get(e)));
				}
			}
		}
		return s;
	}
	public boolean isIsomorphicTo(Expansion other) {
		if (!(other instanceof Substitution)) return false;
		Substitution s1 = this;
		Substitution s2 = (Substitution)other;
		if (s1.size() != s2.size()) return false;
		for (Item.Variable X : keySet()) {
			if (X instanceof Type.SimpleVariable) {
				if (!s1.get(X).equals(s2.get(X)))
					return false;
			}
			else {
				Expansion E1 = (Expansion)s1.get(X);
				Expansion E2 = (Expansion)s2.get(X);
				if (!E1.isIsomorphicTo(E2))
					return false;
			}
		}
		return true;
	}
	public int size() { return map.size(); }
	public String toString() {
		String s = "{";
		int i = 0;
		for (Item.Variable X : map.keySet()) {
			s += X + ":=" + map.get(X);
			i++;
			if (i < map.size())
				s += ", ";
		}
		s += "}";
		return s;
	}
}
