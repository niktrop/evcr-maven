import java.util.*;

public class TermContext extends HashMap<Term.Variable,Type> {
	public TermContext() {}
	public TermContext(TermContext c) { super(c); }
	public TermContext with(Term.Variable x, Type T) {
		TermContext c = new TermContext(this);
		if (c.containsKey(x)) throw new RuntimeException(x+" already in term context");
		c.put(x, T);
		return c;
	}
	public TermContext without(Term.Variable x)
	{ TermContext c = new TermContext(this); c.remove(x); return c; }
	public Set<Item.Variable> ovars() {
		Set<Item.Variable> ovars = new HashSet<Item.Variable>();
		for (Type T : values())
			ovars.addAll(T.ovars());
		return ovars;
	}
	public TermContext applySubstitution(Substitution s) {
		TermContext c = new TermContext();
		for (Term.Variable x : keySet())
			c.put(x, s.apply(get(x)));
		return c;
	}
	public VarStruct varStruct() {
		VarStruct vs = new VarStruct();
		for (Term.Variable x : keySet()) {
			Type T = get(x);
			if (T == null)
				T = new Type.Omega();
			vs = vs.join(T.varStruct());
		}
		return vs;
	}
	public static TermContext intersection(TermContext G1, TermContext G2) {
		TermContext G3 = new TermContext();
		for (Term.Variable x : G1.keySet()) {
			Type T1 = G1.get(x), T2 = G2.get(x);
			if (T2 == null)
				G3.put(x,T1);
			else
				G3.put(x,new Type.Intersection(T1,T2));
		}
		for (Term.Variable x : G2.keySet())
			if (G1.get(x) == null)
				G3.put(x,G2.get(x));
		return G3;
	}
	public static TermContext evar(Expansion.Variable e, TermContext G) {
		TermContext G1 = new TermContext();
		for (Term.Variable x : G.keySet())
			G1.put(x,new Type.EvarApplication(e,G.get(x)));
		return G1;
	}
	public static TermContext sub(Substitution s, TermContext G) {
		TermContext G1 = new TermContext();
		for (Term.Variable x : G.keySet())
			G1.put(x,s.apply(G.get(x)));
		return G1;
	}
	public Set<Type> topTypeVariables() {
		Set<Type> set = new HashSet<Type>();
		for (Type type : values())
			for (Type X : type.topTypeVariables())
				set.add(X);
		return set;
	}
	public TermContext elimOmega() {
		TermContext c = new TermContext(this);
		for (Iterator<Term.Variable> it = keySet().iterator(); it.hasNext(); ) {
			Term.Variable x = it.next();
			Type T = get(x).elimOmega();
			if (T instanceof Type.Omega) remove(x);
			else put(x, T);
		}
		return c;
	}
	public String toString() {
		String str = "";
		Iterator<Term.Variable> it = keySet().iterator();
		while (it.hasNext()) {
			Term.Variable x = it.next();
			Type T = get(x);
			str += x+" : "+T;
			if (it.hasNext()) str += ",  ";
		}
		return str;
	}
}
