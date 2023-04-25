import java.util.*;

public interface Term {
	public static final Unifier u = new OpusBeta();
	Set<Typing> I();
	Term substitute(Variable x, Term s);
	Set<Variable> fv();
	Value eval();
	Value evalTop();
	public interface Value extends Term {
		Typing Iv();
		Term apply(Value v);
	}
	public abstract class Abstract implements Term {
		public boolean equals(Object other) { return toString().equals(other.toString()); }
		public int hashCode() { return toString().hashCode(); }
		public abstract String toString();
	}
	public abstract class Reducible extends Abstract {
		public Value evalTop() {
			Term t = this;
			while (t instanceof Reducible) {
				t = ((Reducible)t).reduce();
				if (Shell.showEval) System.out.println("> " + t);
			}
			System.out.println("result: " + t);
			return (Value)t;
		}
		public Value eval() {
			Term t = this;
			while (t instanceof Reducible)
				t = ((Reducible)t).reduce();
			return (Value)t;
		}
		public abstract Term reduce();
	}
	public abstract class AbstractValue extends Abstract implements Value {
		public Value eval() { return this; }
		public Value evalTop() { return this; }
		public Term apply(Value v) {
			throw new IllegalStateException("This value ("+getClass()+") cannot be applied to " + v);
		}
		public Set<Typing> I() {
			// Default implementation for values
			Expansion.Variable e = new Expansion.Variable();
			Typing typ = Iv().evar(e).elimOmega();
			Set<Typing> typSet = Util.set(typ);
			return typSet;
		}
	}
	public interface Constant extends Term {
		Type getType();
	}
	public class Variable extends AbstractValue implements Comparable<Variable>, ABit {
		private static int counter = 0;
		private String name;
		public Variable() { this("x"+(++counter)); }
		public Variable(String name) { this.name = name; }
		public Term substitute(Variable x, Term s) { return equals(x) ? s : this; }
		public String toString() { return name; }
		public Set<Variable> fv() { return Util.set(this); }
		public int compareTo(Variable other) { return name.compareTo(other.name); }
		public Typing Iv() {
			Type.SimpleVariable X = new Type.SimpleVariable();
			Typing typing = new Typing(new TermContext().with(this,X), X);
			return typing;
		}
	}
	public class Extension extends AbstractValue {
		private Label l;
		private Term extendedValue;
		private Value base;
		public Extension(Label l, Term extendedValue, Value base) {
			this.l = l;
			this.extendedValue = extendedValue;
			this.base = base;
		}
		public Set<Variable> fv() { return Util.sset(extendedValue.fv(), base.fv()); }
		public Term substitute(Variable x, Term s)
		{ return new Extension(l, extendedValue.substitute(x, s), (Value)base.substitute(x, s)); }
		public Term apply(Value v)
		{ return l.equals(v) ? extendedValue : new Application(base, v); }
		public Typing Iv() {
			Set<Typing> typs = new HashSet<Typing>();
			Set<Typing> typs1 = extendedValue.I();
			for (Typing typ1 : typs1) {
				Type T = new Type.Function(new Type.Label(l.name), typ1.type);
				TermContext G = typ1.termContext;
				Typing typ = new Typing(G, T);
				typs.add(typ.renameZero());
			}
			Set<Typing> typs2 = base.I();
			for (Typing typ2 : typs2) {
				Expansion.Variable e1 = new Expansion.Variable();
				Expansion.Variable e2 = new Expansion.Variable();
				Type T = new Type.EvarApplication(e1,
						new Type.Function(
							new Type.SimpleVariable(new LConstraint().with(new Type.Label(l.name))),
							new Type.EvarApplication(e2, new Type.SimpleVariable())
						));
				Set<Substitution> sset = u.U(new UConstraintSet(new UConstraint(T, typ2.type)));
				for (Substitution s : sset)
					typs.add(s.apply(typ2).renameZero());
			}
			Typing result = Typing.isect(typs);
			return result.rename();
		}
		public String toString() { return l+"->"+extendedValue+"^"+base; }
	}
	public class Abstraction extends AbstractValue {
		Variable x;
		Term t;
		public Abstraction(Variable x, Term t) { this.x = x; this.t = t; }
		public Term apply(Value v) { return t.substitute(x, v); }
		public Set<Variable> fv() {
			Set<Variable> set = t.fv();
			set.remove(x);
			return set;
		}
		public Term substitute(Variable y, Term s) {
			if (y.equals(x)) return this;
			else if (!s.fv().contains(x)) return new Abstraction(x, t.substitute(y, s));
			else {
				Variable z = new Variable();
				return new Abstraction(z, t.substitute(x, z)).substitute(y, s);
			}
		}
		public String toString() { return "\\" + x + "." + t; }
		public Typing Iv() {
			Set<Typing> typSet = t.I();
			Set<Typing> oTypSet = new HashSet<Typing>();
			for (Typing typ : typSet) {
				Type T = typ.type; TermContext G = typ.termContext;
				Expansion.Variable e = new Expansion.Variable();
				TermContext G1 = G.without(x);
				Type S = G.get(x);
				if (S == null) S = new Type.Omega();
				Typing typing = new Typing(G1, new Type.Function(S,T)).evar(e);
				oTypSet.add(typing.renameZero());
			}
			Typing result = Typing.isect(oTypSet);
			return result.rename();
		}
	}
	public interface ABit {}
	public class Application extends Reducible {
		private Term t1, t2;
		private boolean let;
		public Application(Term t1, Term t2, boolean let) {
			this.t1 = t1;
			this.t2 = t2;
			this.let = let;
		}
		public Application(Term t1, Term t2) { this(t1, t2, false); }
		public Term reduce() {
			if (t1 instanceof Reducible) return new Application(((Reducible)t1).reduce(), t2);
			else if (t2 instanceof Reducible) return new Application(t1, ((Reducible)t2).reduce());
			else return ((Value)t1).apply((Value)t2);
		}
		public Set<Variable> fv() { return Util.sset(t1.fv(), t2.fv()); }
		public Term substitute(Variable x, Term s)
		{ return new Application(t1.substitute(x, s), t2.substitute(x, s)); }
		public String toString() {
			String s = "";
			if (let) { // Print out as a let term for readability
				Abstraction abs = (Abstraction)t1;
				s += "let " + abs.x + " = " + t2 + " in " + abs.t;
			}
			else {
				if (t1 instanceof Application || t1 instanceof ABit) s += t1;
				else s += "(" + t1 + ")";
				s += " ";
				if (t2 instanceof ABit) s += t2;
				else s += "(" + t2 + ")";
			}
			return s;
		}
		public Set<Typing> I() {
			Set<Typing> typSet1 = t1.I();
			Set<Typing> typSet2 = t2.I();
			Set<Typing> typSet = new HashSet<Typing>();
			for (Typing typ1 : typSet1) {
				Type T1 = typ1.type; TermContext G1 = typ1.termContext;
				for (Typing typ2 : typSet2) {
					Type T2 = typ2.type; TermContext G2 = typ2.termContext;
					Type eX = Type.Encode.TypeVariable();
					Type F = new Type.Function(T2, eX);
					UConstraintSet cset = new UConstraintSet(new UConstraint(T1, F));
					VarStruct vs =
						T1.varStruct().join(F.varStruct()).join(G1.varStruct()).join(G2.varStruct());
					Set<Substitution> sset = u.U(cset, vs);
					for (Substitution s : sset) {
						Typing typing =
							new Typing(TermContext.sub(s,TermContext.intersection(G1,G2)), s.apply(eX));
						typSet.add(typing.renameZero());
					}
				}
			}
			Set<Typing> result = new HashSet<Typing>();
			for (Typing rtyp : typSet)
				result.add(rtyp.rename());
			return result;
		}
	}
	public abstract class AbstractConstant extends AbstractValue implements ABit, Value {
		public abstract Type getType();
		public Term substitute(Variable x, Term s) { return this; }
		public Set<Variable> fv() { return new HashSet<Variable>(); }
		public Typing Iv() { return new Typing(new TermContext(), getType()); }
	}
	public class Label extends AbstractConstant {
		public String name;
		public Label(String name) { this.name = name; }
		public String toString() { return "."+name; }
		public Type getType() { return new Type.Label(name); }
	}
	public class Void extends AbstractConstant {
		public String toString() { return "()"; }
		public Type getType() { return new Type.Void(); }
	}
	public abstract class ValueConstant<Primitive> extends AbstractConstant {
		private Primitive value;
		public ValueConstant(Primitive value) { this.value = value; }
		public Primitive getValue() { return value; }
		public String toString() { return "" + value; }
	}
	public class Int extends ValueConstant<Integer> {
		public Int(int value) { super(value); }
		public Type getType() { return new Type.Int(); }
	}
	public class Real extends ValueConstant<Double> {
		public Real(double value) { super(value); }
		public Type getType() { return new Type.Real(); }
	}
	public class Bool extends ValueConstant<Boolean> {
		public Bool(boolean value) { super(value); }
		public Type getType() { return new Type.Bool(); }
	}
	public class Str extends ValueConstant<String> {
		public Str(String value) { super(value); }
		public Type getType() { return new Type.Str(); }
		public String toString() { return "\"" + super.toString() + "\""; }
	}
	public abstract class SecondaryTerm extends Abstract {
		public Set<Typing> I() { throw new RuntimeException(); }
	}
	public class PrimIfFunction extends AbstractConstant {
		public Type getType() {
			Type X1 = Type.Encode.TypeVariable();
			Type X2 = Type.Encode.TypeVariable();
			Type X3 = Type.Encode.TypeVariable();
			Type X4 = Type.Encode.TypeVariable();
			Expansion.Variable c = new Expansion.Variable();
			Expansion.Variable d = new Expansion.Variable();
			Expansion.Variable h = new Expansion.Variable();
			return new Type.Function(new Type.Intersection( new Type.Intersection(
							new Type.Function(new Type.EvarApplication(new Expansion.Variable(),
									new Type.Function( new Type.EvarApplication(c, new Type.EvarApplication(d, X1)),
										new Type.EvarApplication(c, new Type.Function( new Type.Omega(),
												new Type.EvarApplication(d, new Type.Function(new Type.Omega(), X1)))))
									), new Type.Bool()),
							new Type.Function(new Type.EvarApplication(new Expansion.Variable(),
									new Type.Function(new Type.Omega(), new Type.EvarApplication(
											new Expansion.Variable(), new Type.Function( new Type.EvarApplication(h, X2),
												new Type.EvarApplication(h, new Type.Function( new Type.Omega() , X2)))))), X4)),
						new Type.Function(new Type.EvarApplication(new Expansion.Variable(), new Type.Function(
									new Type.Omega(), new Type.EvarApplication(new Expansion.Variable(),
										new Type.Function( new Type.Omega(), new Type.EvarApplication(
												new Expansion.Variable(), new Type.Function(X3, X3)))))), X4)), X4);
		}
		public String toString() { return "$primif"; }
		public Term apply(Value f) {
			Variable x = new Variable("$x"), y = new Variable("$y"), z = new Variable("$z");
			Value v1 = f.apply(new Abstraction(x, new Abstraction(y, new Abstraction(z,x)))).eval();
			Term t1 = f.apply(new Abstraction(x, new Abstraction(y, new Abstraction(z,y))));
			Term t2 = f.apply(new Abstraction(x, new Abstraction(y, new Abstraction(z,z))));
			return apply(v1, t1, t2);
		}
		public Term apply(Value a, Term b, Term c) {
			if (!(a instanceof Bool)) throw new RuntimeException();
			boolean condition = ((Bool)a).getValue().booleanValue();
			return condition ? b : c;
		}
	}
	public class PlusFunction extends BinaryFunction {
		public Type[] getTypes()
		{ return new Type[] { new Type.Int(), new Type.Int(), new Type.Int() }; }
		public String toString() { return "add"; }
		public Value apply(Value a, Value b) {
			if (a instanceof Int) return new Int(((Int)a).getValue() + ((Int)b).getValue());
			else if (a instanceof Real) return new Real(((Real)a).getValue() + ((Real)b).getValue());
			else if (a instanceof Str) return new Str(((Str)a).getValue() + b);
			else throw new RuntimeException();
		}
	}
	public class CatFunction extends BinaryFunction {
		public Type[] getTypes()
		{ return new Type[] { new Type.Str(), new Type.Str(), new Type.Str() }; }
		public String toString() { return "cat"; }
		public Value apply(Value a, Value b) {
			String s1 = (String)(((Str)a).getValue());
			String s2 = (String)(((Str)b).getValue());
			return new Str(s1 + s2);
		}
	}
	public class MinusFunction extends BinaryFunction {
		public Type[] getTypes()
		{ return new Type[] { new Type.Int(), new Type.Int(), new Type.Int() }; }
		public String toString() { return "sub"; }
		public Value apply(Value a, Value b)
		{
			if (a instanceof Int) return new Int(((Int)a).getValue() - ((Int)b).getValue());
			else if (a instanceof Real) return new Real(((Real)a).getValue() - ((Real)b).getValue());
			else throw new RuntimeException();
		}
	}
	public abstract class BinaryFunction extends AbstractConstant {
		protected abstract Type[] getTypes();
		public Type getType() {
			Type[] types = getTypes();
			Expansion.Variable e1 = new Expansion.Variable();
			Type X1 = Type.Encode.TypeVariable();
			Type X2 = Type.Encode.TypeVariable();
			return new Type.Function(new Type.Intersection(new Type.Function(new Type.EvarApplication(
								new Expansion.Variable(), new Type.Function(new Type.EvarApplication(e1,X1),
										new Type.EvarApplication(e1,new Type.Function(new Type.Omega(),X1)))), types[0]),
						new Type.Function(new Type.EvarApplication(new Expansion.Variable(), new Type.Function(
										new Type.Omega(), new Type.EvarApplication(new Expansion.Variable(),
											new Type.Function(X2,X2)))), types[1])),
					new Type.EvarApplication(new Expansion.Variable(), types[2]));
		}
		public Term apply(Value f)
		{ return apply(f.apply(Encode.True()).eval(), f.apply(Encode.False()).eval()); }
		public abstract Value apply(Value a, Value b);
	}
	public class MulFunction extends BinaryFunction {
		public Type[] getTypes()
		{ return new Type[] { new Type.Int(), new Type.Int(), new Type.Int() }; }
		public String toString() { return "_mul"; }
		public Value apply(Value a, Value b) {
			if (a instanceof Int) return new Int(((Int)a).getValue() * ((Int)b).getValue());
			else if (a instanceof Real) return new Real(((Real)a).getValue() * ((Real)b).getValue());
			else throw new RuntimeException();
		}
	}
	public class DivFunction extends AbstractConstant {
		public Type getType() {
			return new Type.Function(new Type.Int(),new Type.Function(new Type.Int(),new Type.Int()));
		}
		public String toString() { return "_div"; }
		public Term op(Term a, Term b) {
			if (a instanceof Int) return new Int(((Int)a).getValue() / ((Int)b).getValue());
			else if (a instanceof Real) return new Real(((Real)a).getValue() / ((Real)b).getValue());
			else throw new RuntimeException();
		}
	}
	public class AndFunction extends BinaryFunction {
		public Type[] getTypes()
		{ return new Type[] { new Type.Bool(), new Type.Bool(), new Type.Bool() }; }
		public String toString() { return "_and"; }
		public Value apply(Value a, Value b)
		{ return new Bool(((Bool)a).getValue() && ((Bool)b).getValue()); }
	}
	public class OrFunction extends AbstractConstant {
		public Type getType() {
			return new Type.Function(new Type.Bool(),
					new Type.Function(new Type.Bool(),new Type.Bool()));
		}
		public String toString() { return "_or"; }
		public Term op(Term a, Term b)
		{ return new Bool(((Bool)a).getValue() || ((Bool)b).getValue()); }
	}
	public class NotFunction extends AbstractConstant {
		public NotFunction() {}
		public Term apply(Value argument) { return new Bool(!((Bool)argument).getValue()); }
		public String toString() { return "_not"; }
		public Type getType() {
			return new Type.Function(new Type.Bool(),
					new Type.EvarApplication(new Expansion.Variable(), new Type.Bool()));
		}
	}
	public class StrFunction extends AbstractConstant {
		public StrFunction() {}
		public Term apply(Value argument) { return new Str("" + ((Int)argument).getValue()); }
		public String toString() { return "str"; }
		public Type getType() {
			return new Type.Function(new Type.Int(),
					new Type.EvarApplication(new Expansion.Variable(), new Type.Str()));
		}
	}
	public abstract class IntCompFunction extends BinaryFunction {
		public Type[] getTypes()
		{ return new Type[] { new Type.Int(), new Type.Int(), new Type.Bool() }; }
	}
	public class LtFunction extends IntCompFunction {
		public String toString() { return "_lt"; }
		public Value apply(Value a, Value b)
		{ return new Bool(((Int)a).getValue() < ((Int)b).getValue()); }
	}
	public class GtFunction extends IntCompFunction {
		public String toString() { return "_gt"; }
		public Value apply(Value a, Value b)
		{ return new Bool(((Int)a).getValue() > ((Int)b).getValue()); }
	}
	public class LeFunction extends IntCompFunction {
		public String toString() { return "_le"; }
		public Value apply(Value a, Value b)
		{ return new Bool(((Int)a).getValue() <= ((Int)b).getValue()); }
	}
	public class GeFunction extends IntCompFunction {
		public String toString() { return "_ge"; }
		public Value apply(Value a, Value b)
		{ return new Bool(((Int)a).getValue() >= ((Int)b).getValue()); }
	}
	public class EqFunction extends BinaryFunction {
		public Type[] getTypes() {
			Expansion.Variable e = new Expansion.Variable();
			Type.SimpleVariable h = new Type.SimpleVariable();
			Type.EvarApplication e_h = new Type.EvarApplication(e, h);
			return new Type[] { h, h, new Type.Bool() };
		}
		// Overridden
		public Type getType() {
			Type[] types = getTypes();
			Expansion.Variable e1 = new Expansion.Variable();
			Type X1 = Type.Encode.TypeVariable(), X2 = Type.Encode.TypeVariable();
			return new Type.Function(new Type.Intersection(new Type.Function(new Type.EvarApplication(
								new Expansion.Variable(), new Type.Function(new Type.EvarApplication(e1,X1),
										new Type.EvarApplication(e1,new Type.Function(new Type.Omega(),X1)))),
								Type.Encode.TypeVariable()),
						new Type.Function(new Type.EvarApplication(new Expansion.Variable(), new Type.Function(
										new Type.Omega(), new Type.EvarApplication(new Expansion.Variable(),
											new Type.Function(X2,X2)))), Type.Encode.TypeVariable())),
					new Type.EvarApplication(new Expansion.Variable(), types[2]));
		}
		public String toString() { return "_eq"; }
		public Value apply(Value a, Value b) { return new Bool(a.equals(b)); }
	}
	public class PrintlnFunction extends AbstractConstant {
		public Term apply(Value argument) {
			System.out.println(argument);
			return new Void();
		}
		public String toString() { return "$println"; }
		public Type getType() {
			return new Type.Function(new Type.EvarApplication(new Expansion.Variable(),
						new Type.SimpleVariable()),new Type.Omega());
		}
	}
	public class Encode {
		public static Term eta(Variable x, Term t)
		{ return new Abstraction(x, new Application(t,x)); }
		public static Term Let(Variable x, Term s, Term t)
		{ return new Application(new Abstraction(x,t),s); }
		public static Value True() {
			Variable x = new Variable(), y = new Variable();
			return new Abstraction(x, new Abstraction(y, x));
		}
		public static Value False() {
			Variable x = new Variable(), y = new Variable();
			return new Abstraction(x, new Abstraction(y, y));
		}
		public static Value Identity() {
			Variable x = new Variable("x");
			return new Abstraction(x, x);
		}
		public static Term Tripple(Term a, Term b, Term c) {
			// (\x.\y.\z.\f.f x y z) a b c
			Variable x = new Variable("x"), y = new Variable("y"),
												z = new Variable("z"), f = new Variable("f");
			return new Application(new Application(new Application(new Abstraction(x,
								new Abstraction(y, new Abstraction(z, new Abstraction(f, new Application(
												new Application(new Application(f, x), y), z))))), a), b), c);
		}
		public static Term Pair(Term a, Term b) {
			// (\x.\y.\f.f x y) a b
			Variable x = new Variable("x"), y = new Variable("y"), f = new Variable("f");
			return new Application( new Application( new Abstraction(x, new Abstraction(y,
							new Abstraction( f, new Application( new Application(f, x), y)))), a), b);
		}
	}
}
