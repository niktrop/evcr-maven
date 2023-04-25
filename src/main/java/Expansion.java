public interface Expansion extends Item<Expansion> {
	<X extends Item<X>> X apply(X item);
	Expansion restrictedTo(VarStruct vs);
	boolean isIsomorphicTo(Expansion other);

	public class Variable implements Item.Variable {
		static class Sequence extends NameSequence {
			public String next() {
				String next = super.next();
				if (next.equals("w")) // reserved for omega
					next = super.next();
				return next;
			}
		}
		private static NameSequence names = new Sequence();
		public static NameSequence getNameSequence() { return names; }
		public static void setNameSequence(NameSequence newNames) { names = newNames; }
		public static NameSequence resetNameSequence()
		{ NameSequence old = names; names = new Sequence(); return old; }
		private String name;
		public Variable() { this(names.next()); }
		public Variable(String name) { this.name = name; }
		public String toString() { return name; }
	}
	public abstract class Abstract implements Expansion {
		public Expansion get() { return this; }
		public static Intersection _intersection(Expansion item1, Expansion item2)
		{ return new Intersection(item1, item2); }
		public Intersection intersection(Expansion item1, Expansion item2)
		{ return _intersection(item1, item2); }
		public static EvarApplication _evarApplication(Expansion.Variable e, Expansion K)
		{ return new EvarApplication(e, K); }
		public EvarApplication evarApplication(Expansion.Variable e, Expansion K)
		{ return _evarApplication(e, K); }
		public static Omega _omega() { return new Omega(); }
		public Omega omega() { return _omega(); }
	}
	public class Intersection extends Item.Intersection<Expansion> implements Expansion {
		public Expansion get() { return this; }
		public Intersection(Expansion left, Expansion right)
		{ super(left, right); }
		public Expansion.Intersection intersection(Expansion item1, Expansion item2)
		{ return Expansion.Abstract._intersection(item1, item2); }
		public Expansion.EvarApplication evarApplication(Expansion.Variable e, Expansion K)
		{ return Expansion.Abstract._evarApplication(e, K); }
		public Expansion.Omega omega()
		{ return Expansion.Abstract._omega(); }
		public <X extends Item<X>> X apply(X item)
		{ return item.intersection(left.apply(item), right.apply(item)); }
		public String toString() {
			return Util.brac(left, left instanceof Expansion.Intersection) + " ^ " + right;
		}
		public Expansion restrictedTo(VarStruct vs)
		{ return new Expansion.Intersection(left.restrictedTo(vs), right.restrictedTo(vs)); }
		public boolean isIsomorphicTo(Expansion other) {
			if (!(other instanceof Expansion.Intersection)) return false;
			Expansion.Intersection Ei = (Expansion.Intersection)other;
			return left.isIsomorphicTo(Ei.left) && right.isIsomorphicTo(Ei.right);
		}
	}
	public class EvarApplication extends Item.EvarApplication<Expansion> implements Expansion {
		public Expansion get() { return this; }
		public EvarApplication(Expansion.Variable e, Expansion K)
		{ super(e, K); }
		public Expansion.Intersection intersection(Expansion item1, Expansion item2)
		{ return Expansion.Abstract._intersection(item1, item2); }
		public Expansion.EvarApplication evarApplication(Expansion.Variable e, Expansion K)
		{ return Expansion.Abstract._evarApplication(e, K); }
		public Expansion.Omega omega()
		{ return Expansion.Abstract._omega(); }
		public <X extends Item<X>> X apply(X item)
		{ return item.evarApplication(e, K.apply(item)); }
		public String toString()
		{ return e + " " + Util.brac(K, K instanceof Expansion.Intersection); }
		public Expansion restrictedTo(VarStruct vs)
		{ return new Expansion.EvarApplication(e, K.restrictedTo(vs)); }
		public boolean isIsomorphicTo(Expansion other) {
			if (!(other instanceof Expansion.EvarApplication)) return false;
			Expansion.EvarApplication Ee = (Expansion.EvarApplication)other;
			return K.isIsomorphicTo(Ee.K);
		}
	}
	public class Omega extends Item.Omega<Expansion> implements Expansion {
		public Expansion get() { return this; }
		public Expansion.Intersection intersection(Expansion item1, Expansion item2)
		{ return Expansion.Abstract._intersection(item1, item2); }
		public Expansion.EvarApplication evarApplication(Expansion.Variable e, Expansion K)
		{ return Expansion.Abstract._evarApplication(e, K); }
		public Expansion.Omega omega()
		{ return Expansion.Abstract._omega(); }
		public <X extends Item<X>> X apply(X item)
		{ return item.omega(); }
		public Expansion restrictedTo(VarStruct vs) { return this; }
		public boolean isIsomorphicTo(Expansion other)
		{ return other instanceof Expansion.Omega; }
	}
}
