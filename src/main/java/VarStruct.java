import java.util.*;

public class VarStruct {
	private Map<Item.Variable, VarStruct> map = new HashMap<Item.Variable, VarStruct>();
	private List<Item.Variable> keys = new ArrayList<Item.Variable>();
	private Map<Item.Variable, Boolean> pureMap = new HashMap<Item.Variable, Boolean>();
	public Set<Item.Variable> ovars() {
		Set<Item.Variable> ovars = new HashSet<Item.Variable>();
		ovars.addAll(keys);
		return ovars;
	}
	public int size() { return keys.size(); }
	public void put(Item.Variable X, VarStruct varStruct, Boolean pure) {
		if (X instanceof Expansion.Variable && varStruct == null) throw new NullPointerException();
		map.put(X, varStruct);
		keys.add(X);
		pureMap.put(X, pure);
	}
	public VarStruct get(Item.Variable X) { return map.get(X); }
	public Boolean pure(Item.Variable X) { return pureMap.get(X); }
	public boolean containsKey(Item.Variable X) { return map.containsKey(X); }
	public VarStruct join(VarStruct other) {
		VarStruct result = new VarStruct();
		List<Item.Variable> vars = new ArrayList<Item.Variable>(keys);
		for (Item.Variable X : other.keys)
			if (!map.containsKey(X))
				vars.add(X);
		for (Item.Variable X : vars) {
			VarStruct vs1 = get(X), vs2 = other.get(X);
			if (vs1 != null && vs2 != null)
				result.put(X, vs1.join(vs2), pure(X) && other.pure(X));
			else if (vs1 != null)
				result.put(X, vs1, pure(X));
			else
				result.put(X, vs2, other.pure(X));
		}
		return result;
	}
	public Substitution eliminateRedundantEvars() {
		Substitution s = new Substitution();
		for (Item.Variable X : keys) {
			if (X instanceof Expansion.Variable) {
				Expansion.Variable e = (Expansion.Variable)X;
				if (pure(X))
					s.put(e, get(X).eliminateRedundantEvars());
				else
					s.put(e, new Expansion.EvarApplication(e, get(X).eliminateRedundantEvars()));
			}
		}
		return s;
	}
	public Substitution renaming() { return _renaming(true); }
	private Substitution _renaming(boolean root) {
		Substitution s = new Substitution();
		NameSequence oldVariableNames = null;
		if (!root) oldVariableNames = Type.SimpleVariable.resetNameSequence();
		for (Item.Variable X : keys) {
			if (X instanceof Expansion.Variable) {
				Expansion.Variable e = (Expansion.Variable)X;
				Expansion.Variable e1 = new Expansion.Variable();
				s.put(e, new Expansion.EvarApplication(e1, get(X)._renaming(false)));
			}
			else if (X instanceof Type.SimpleVariable) {
				Type.SimpleVariable H = (Type.SimpleVariable)X;
				Type.SimpleVariable H1 = new Type.SimpleVariable(H.constraint);
				s.put(H, H1);
			}
		}
		if (!root) Type.SimpleVariable.setNameSequence(oldVariableNames);
		return s;
	}
	public VarStruct applySubstitution(Substitution s) {
		VarStruct vs = new VarStruct();
		for (Item.Variable X : keys) {
			if (X instanceof Expansion.Variable) {
				Expansion E = (Expansion)s.get(X);
				VarStruct _varStruct;
				if (E != null)
					_varStruct = get(X).applyExpansion(E);
				else {
					_varStruct = new VarStruct();
					_varStruct.put(X, get(X), pure(X));
				}
				vs = vs.join(_varStruct);
			}
			else {
				Type T = (Type)s.get(X);
				if (T instanceof Type.SimpleVariable)
					vs.put((Type.SimpleVariable)T, null, new Boolean(false));
				else
					vs.put(X, get(X), pure(X));
			}
		}
		return vs;
	}
	public VarStruct applyExpansion(Expansion E) {
		if (E instanceof Substitution) return ((Substitution)E).apply(this);
		else if (E instanceof Expansion.Omega) return new VarStruct();
		else if (E instanceof Expansion.Intersection) {
			Expansion.Intersection Ei = (Expansion.Intersection)E;
			return applyExpansion(Ei.left).join(applyExpansion(Ei.right));
		}
		else if (E instanceof Expansion.EvarApplication) {
			Expansion.EvarApplication e_E = (Expansion.EvarApplication)E;
			VarStruct vs = new VarStruct();
			vs.put(e_E.e, applyExpansion(e_E.K), false);
			return vs;
		}
		else
			throw new IllegalStateException("Invalid expansion: " + E);
	}
	public String toString() {
		String s = "{", delim = "";
		for (Item.Variable X : keys) {
			s += delim;
			delim = ",";
			s += X;
			if (pure(X)) s += "* ";
			if (get(X) != null) s += get(X);
		}
		s += "}";
		return s;
	}
}
