import java.util.*;

public interface Type extends Item<Type> {
	Set<Variable> ovars();
	boolean isOmega();
	boolean isIsomorphicTo(Type other);
	Type rename();
	Type elimOmega();
	VarStruct varStruct();
	int size();
	int rank();
	Set<Type> topTypeVariables();
	Set<Type> getComponents();
	public interface Simple extends Type {
		boolean meets(LConstraint constraint);
	}
	public interface Constant extends Simple {}
	public abstract class Abstract implements Type {
		public Type get() { return this; }
		// for example, e w is omega due to type equivalences.
		public boolean isOmega() { return false; }
		public Type elimOmega() { return this; }
		public Set<Type> topTypeVariables() { return new HashSet<Type>(); }
		public Set<Type> getComponents() { return Util.set((Type)this); }
		public boolean equals(Object other)
		{ return toString().equals(other.toString()); }
		public String toString() { return getClass().toString(); }
		public int hashCode() { return toString().hashCode(); }
		public static Intersection _intersection(Type item1, Type item2)
		{ return new Intersection(item1, item2); }
		public Intersection intersection(Type item1, Type item2)
		{ return _intersection(item1, item2); }
		public static EvarApplication _evarApplication(Expansion.Variable e, Type K)
		{ return new EvarApplication(e, K); }
		public EvarApplication evarApplication(Expansion.Variable e, Type K)
		{ return _evarApplication(e, K); }
		public static Omega _omega() { return new Omega(); }
		public Omega omega() { return _omega(); }
	}
	public abstract class AbstractSimple extends Abstract implements Simple
	{ public boolean meets(LConstraint constraint) { return constraint.size() == 0; }; }
	// Constrained simple type variable
	public class SimpleVariable extends AbstractSimple implements Variable {
		static class Sequence extends NameSequence {
			private String next = "";
			public String next() {
				String value = next;
				next = super.next().toUpperCase();
				return value;
			}
			public void reset() {
				next = "";
				super.reset();
			}
		}
		private static NameSequence names = new Sequence();
		public static NameSequence getNameSequence() { return names; }
		public static void setNameSequence(NameSequence newNames) { names = newNames; }
		public static NameSequence resetNameSequence()
		{ NameSequence old = names; names = new Sequence(); return old; }

		public String name;
		public LConstraint constraint;
		public int size() { return 1; }
		public int rank() { return 0; }
		public SimpleVariable() { this(new LConstraint()); }
		public SimpleVariable(LConstraint constraint)
		{ this.name = names.next(); this.constraint = constraint; }
		public Set<Variable> ovars() { return Util.set((Variable)this); }
		public Type applySubstitution(Substitution s) {
			Type T = (Type)s.get(this);
			return T != null ? T : this;
		}
		public boolean meets(LConstraint constraint)
		{ return this.constraint.containsAll(constraint); }
		public String toString()
		{ return name+"[" + (constraint.size()>0 ? constraint : "") + "]"; }
		public Type rename() { return this; }
		public boolean isIsomorphicTo(Type other) { return equals(other); }
		public VarStruct varStruct() {
			VarStruct vs = new VarStruct();
			vs.put(this, null, false);
			return vs;
		}
	}
	public abstract class AbstractConstant extends AbstractSimple implements Constant {
		public int size() { return 1; }
		public int rank() { return 0; }
		public Set<Variable> ovars() { return new HashSet<Variable>(); }
		public Type applySubstitution(Substitution s) { return this; }
		public Type elimOmega() { return this; };
		public Type rename() { return this; }
		public boolean isIsomorphicTo(Type other) { return equals(other); }
		public VarStruct varStruct() { return new VarStruct(); }
	}
	public class Label extends AbstractConstant {
		private String name;
		public Label(String name) { this.name = name; }
		public String toString() { return "."+name; }
		public boolean meets(LConstraint constraint) { return !constraint.contains(this); }
	}
	public class Void extends AbstractConstant { public String toString() { return "()"; } }
	public class Int extends AbstractConstant { public String toString() { return "Int"; } }
	public class Real extends AbstractConstant { public String toString() { return "Real"; } }
	public class Bool extends AbstractConstant { public String toString() { return "Bool"; } }
	public class Str extends AbstractConstant { public String toString() { return "Str"; } }
	public class Function extends AbstractSimple {
		public int size() { return parameterType.size() + returnType.size(); }
		public int rank() {
			int r1 = parameterType.rank(), r2 = returnType.rank();
			return (r1 == 0 && r2 == 0) ? 0 : Math.max(1 + r1, r2);
		}
		public Type parameterType, returnType;
		public Function(Type parameterType, Type returnType) {
			this.parameterType = parameterType;
			this.returnType = returnType;
		}
		public Type getParameterType() { return parameterType; }
		public Type getReturnType() { return returnType; }
		public Set<Variable> ovars()
		{ return Util.sset(parameterType.ovars(),returnType.ovars()); }
		public String toString() {
			return Util.brac(parameterType,
					parameterType instanceof Intersection || parameterType instanceof Function)
				+ " -> "
				+ Util.brac(returnType, returnType instanceof Intersection);
		}
		public Type applySubstitution(Substitution s)
		{ return new Function(s.apply(parameterType), s.apply(returnType)); }
		public Type elimOmega()
		{ return new Function(parameterType.elimOmega(), returnType.elimOmega()); }
		public Type rename()
		{ return new Function(parameterType.rename(), returnType.rename()); }
		public boolean isIsomorphicTo(Type other) {
			if (!(other instanceof Function)) return false;
			Function Tf = (Function)other;
			return parameterType.isIsomorphicTo(Tf.parameterType)
				&& returnType.isIsomorphicTo(Tf.returnType);
		}
		public VarStruct varStruct()
		{ return parameterType.varStruct().join(returnType.varStruct()); }
	}
	public class Omega extends Item.Omega<Type> implements Type {
		public int size() { return 1; }
		public int rank() { return 0; }
		public Set<Variable> ovars() { return new HashSet<Variable>(); }
		public boolean isOmega() { return true; }
		public Set<Type> getComponents() { return new HashSet<Type>(); }
		public Set<Type> topTypeVariables() { return new HashSet<Type>(); }
		public Type get() { return this; }
		public Type.Intersection intersection(Type item1, Type item2)
		{ return Type.Abstract._intersection(item1, item2); }
		public Type.EvarApplication evarApplication(Expansion.Variable e, Type K)
		{ return Type.Abstract._evarApplication(e, K); }
		public Type.Omega omega()
		{ return Type.Abstract._omega(); }
		public Type rename() { return this; }
		public boolean isIsomorphicTo(Type other) { return other instanceof Type.Omega; }
		public VarStruct varStruct() { return new VarStruct(); }
	}
	public class Intersection extends Item.Intersection<Type> implements Type {
		public int size() { return 2 * (left.size() + right.size()); }
		public int rank() {
			int r1 = left.rank(), r2 = right.rank();
			return r1 == 0 && r2 == 0 ? 1 : Math.max(r1, r2);
		}
		public Intersection(Type left, Type right) { super(left, right); }
		public Set<Variable> ovars() { return Util.sset(left.ovars(), right.ovars()); }
		public boolean isOmega() { return left.isOmega() && right.isOmega(); }
		public Set<Type> getComponents()
		{ return Util.union(left.getComponents(), right.getComponents()); }
		public Set<Type> topTypeVariables()
		{ return Util.union(left.topTypeVariables(), right.topTypeVariables()); }
		public String toString() {
			return Util.brac(left, left instanceof Function || left instanceof Type.Intersection)
				+ " ^ " + Util.brac(right, right instanceof Function);
		}
		public Type get() { return this; }
		public Type.Intersection intersection(Type item1, Type item2)
		{ return Type.Abstract._intersection(item1, item2); }
		public Type.EvarApplication evarApplication(Expansion.Variable e, Type K)
		{ return Type.Abstract._evarApplication(e, K); }
		public Type.Omega omega()
		{ return Type.Abstract._omega(); }
		public Type rename() { return new Type.Intersection(left.rename(), right.rename()); }
		public boolean isIsomorphicTo(Type other) {
			if (!(other instanceof Type.Intersection)) return false;
			Type.Intersection Ti = (Type.Intersection)other;
			return left.isIsomorphicTo(Ti.left) && right.isIsomorphicTo(Ti.right);
		}
		public VarStruct varStruct() { return left.varStruct().join(right.varStruct()); }
	}
	public class EvarApplication extends Item.EvarApplication<Type> implements Type {
		public int size() { return 1 + K.size(); }
		public int rank() { return K.rank(); }
		public EvarApplication(Expansion.Variable e, Type K)
		{ super(e, K); }
		public boolean isOmega() { return K.isOmega(); }
		public Set<Variable> ovars() { return Util.set((Variable)e); }
		public Set<Type> getComponents() {
			Set<Type> components = new HashSet<Type>();
			for (Type comp : K.getComponents())
				components.add(new Type.EvarApplication(e, comp));
			return components;
		}
		public Set<Type> topTypeVariables() {
			if (Encode.isTypeVariable(this))
				return Util.set((Type)this);
			else {
				Set<Type> inner = K.topTypeVariables();
				Set<Type> set = new HashSet<Type>();
				for (Type i : inner)
					set.add(new Type.EvarApplication(e, i));
				return set;
			}
		}
		public String toString() {
			boolean brac = K instanceof Type.Intersection || K instanceof Function;
			boolean needSpace = brac || K instanceof Type.EvarApplication
				|| K instanceof Constant;
			needSpace = true;
			return e + (needSpace ? " " : "") + Util.brac(K, brac);
		}
		public Type get() { return this; }
		public Type.Intersection intersection(Type item1, Type item2)
		{ return Type.Abstract._intersection(item1, item2); }
		public Type.EvarApplication evarApplication(Expansion.Variable e, Type K)
		{ return Type.Abstract._evarApplication(e, K); }
		public Type.Omega omega()
		{ return Type.Abstract._omega(); }
		public Type rename()
		{ return new Type.EvarApplication(new Expansion.Variable(), K.rename()); }
		public boolean isIsomorphicTo(Type other) {
			if (!(other instanceof Type.EvarApplication)) return false;
			Type.EvarApplication Te = (Type.EvarApplication)other;
			return K.isIsomorphicTo(Te.K);
		}
		public VarStruct varStruct() {
			VarStruct vs = new VarStruct();
			vs.put(e, K.varStruct(), K instanceof Type.EvarApplication);
			return vs;
		}
	}
	public class Encode {
		public static Expansion getTvarPrefix(Type T) {
			if (!(T instanceof EvarApplication)) throw new RuntimeException();
			if (isTypeVariable(T)) return new Substitution();
			else {
				EvarApplication e_T = (EvarApplication)T;
				Expansion.Variable e = e_T.e;
				Type _T = e_T.K;
				return new Expansion.EvarApplication(e, getTvarPrefix(_T));
			}
		}
		public static boolean isTypeVariable(Type T) {
			return T instanceof EvarApplication
				&& ((EvarApplication)T).K instanceof SimpleVariable
				&& ((SimpleVariable)((EvarApplication)T).K).constraint.size() == 0;
		}
		public static Type TypeVariable()
		{ return new EvarApplication(new Expansion.Variable(), new SimpleVariable()); }
		public static Type Pair(Type T1, Type T2) {
			Type X = TypeVariable();
			return new Function(new Function(T1, new Function(T2, X)), X);
		}
	}
}
