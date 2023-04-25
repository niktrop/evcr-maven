import java.util.*;

public class Typing {
	public TermContext termContext;
	public Type type;
	public Typing(TermContext termContext, Type type) {
		this.termContext = termContext;
		if (termContext == null) throw new IllegalStateException();
		this.type = type;
	}
	public int rank() {
		int rank = type.rank();
		for (Type T : termContext.values())
			if (T.rank() > rank)
				rank = T.rank();
		return rank;
	}
	public static int rankSet(Set<Typing> set) {
		int rank = 0;
		for (Typing typ : set)
			if (typ.rank() > rank)
				rank = typ.rank();
		return rank;
	}
	public Typing applySubstitution(Substitution s)
	{ return new Typing(s.apply(termContext),s.apply(type)); }
	public Typing evar(Expansion.Variable e)
	{ return new Typing(TermContext.evar(e, termContext), new Type.EvarApplication(e, type)); }
	public String toString() {
		String s = type.toString();
		if (termContext.size() > 0)
			s += " <| " + termContext;
		return s;
	}
	public String toString(boolean open)
	{ return type + (open ? (" <| "+termContext) : ""); }
	public boolean equals(Object other)
	{ return other != null && other.toString().equals(toString()); }
	public int hashCode() { return toString().hashCode(); }
	public Typing estrip() {
		Typing typ = null;
		if (type instanceof Type.EvarApplication) {
			Type.EvarApplication e_T = (Type.EvarApplication)type;
			TermContext G = new TermContext();
			for (Term.Variable x : termContext.keySet()) {
				Type S = termContext.get(x);
				if (S == null || S.isOmega()) continue;
				if (S instanceof Type.EvarApplication) {
					Type.EvarApplication e_S = (Type.EvarApplication)S;
					if (e_S.e.equals(e_T.e)) G.put(x, e_S.K);
					else { G = null; break; }
				}
				else { G = null; break; }
			}
			if (G != null) typ = new Typing(G, e_T.K);
		}
		return (typ != null) ? typ : this;
	}
	public static Typing isect(Set<Typing> typings) {
		typings = new HashSet<Typing>(typings);
		Iterator<Typing> it = typings.iterator();
		while (it.hasNext()) {
			Typing typ = it.next().elimOmega();
			if (typ.type instanceof Type.Omega)
				it.remove();
		}
		switch (typings.size()) {
			case 0: return new Typing(new TermContext(), new Type.Omega());
			case 1: return typings.iterator().next().estrip();
			default:
				Set<Typing> typs1 = new HashSet<Typing>(), typs2 = new HashSet<Typing>();
				int half = typings.size() / 2, i = 0;
				for (Typing typ : typings) {
					if (i < half) typs1.add(typ);
					else		  typs2.add(typ);
					i++;
				}
				Expansion.Variable e1 = new Expansion.Variable(), e2 = new Expansion.Variable();
				Typing typ1 = isect(typs1).evar(e1), typ2 = isect(typs2).evar(e2);
				TermContext G = TermContext.intersection(typ1.termContext, typ2.termContext);
				Type T = new Type.Intersection(typ1.type, typ2.type);
				return new Typing(G, T);
		}
	}
	public Typing elimOmega() {
		TermContext G = new TermContext();
		for (Term.Variable x : termContext.keySet()) {
			Type T = termContext.get(x);
			if (T != null && !T.isOmega())
				G.put(x, T.elimOmega());
		}
		return new Typing(G, type.elimOmega());
	}
	public VarStruct varStruct() {
		VarStruct varStruct = type.varStruct();
		List<Term.Variable> xs = new ArrayList<Term.Variable>(termContext.keySet());
		Collections.sort(xs);
		for (Term.Variable x : xs)
			varStruct = varStruct.join(termContext.get(x).varStruct());
		return varStruct;
	}
	public Substitution renaming() { return varStruct().renaming(); }
	public Typing rename() { return renaming().apply(this); }
	public Typing renameZero() {
		NameSequence oldSequence = Expansion.Variable.resetNameSequence();
		Typing typ = rename();
		Expansion.Variable.setNameSequence(oldSequence);
		return typ;
	}
	public static Set<Typing> renameZeroSet(Set<Typing> typs) {
		Set<Typing> result = new HashSet<Typing>();
		for (Typing typ : typs)
			result.add(typ.renameZero());
		return result;
	}
	public static String typingsStr(Set<Typing> typs) {
		String s = "";
		for (Typing typ : typs)
			s += ": " + typ + "\n";
		return s;
	}
}
