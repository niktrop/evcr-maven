import java.util.*;

public class OpusBeta implements Unifier {
	public static boolean TRACE = false;
	public static boolean originalEI = false;
	public static boolean originalEE = false;
	public Set<Substitution> U(UConstraintSet constraints)
	{ return unify(constraints); }
	public Set<Substitution> U(UConstraintSet constraints, VarStruct vs)
	{ return unify(constraints, vs); }
	protected Set<Substitution> _U(UConstraintSet constraints) {
		Set<Substitution> result = unify(constraints);
		return result;
	}
	private Set<Substitution> unify(UConstraintSet constraints) {
		VarStruct vs = new VarStruct();
		for (UConstraint constraint : constraints)
			vs = vs.join(constraint.T1.varStruct()).join(constraint.T2.varStruct());
		return unify(constraints, vs);
	}
	private Set<Substitution> unify(UConstraintSet constraints, VarStruct vs) {
		UConstraint constraint = constraints.iterator().next();
		Type T1 = constraint.T1, T2 = constraint.T2;
		return U(constraints, T1, T2, vs);
	}
	public Set<Substitution> U(final UConstraintSet constraints,
			final Type T1, final Type T2, final VarStruct varStruct) {
		if (constraints.solved())
			return Util.set(new Substitution());
		// We enumerate depth first
		final Set<Substitution> ss = new HashSet<Substitution>();
		Set<UConstraintSet> csets = rfactor(constraints);
		for (final UConstraintSet cset : csets) {
			UConstraint constraint = pickUnsolved(cset);
			for (Substitution s1 : opus(constraint, varStruct)) {
				UConstraintSet next_cset = s1.apply(cset).simplify();
				Type nextT1 = s1.apply(T1), nextT2 = s1.apply(T2);
				VarStruct nextVarStruct = s1.apply(varStruct);
				int max_rank = 10; // Halt if we exceed this rank
				if (nextT1.rank() > max_rank)
					throw new RuntimeException(nextT1 + " has rank " + nextT1.rank());
				if (nextT2.rank() > max_rank)
					throw new RuntimeException(nextT2 + " has rank " + nextT2.rank());
				for (Substitution s2 : U(next_cset, nextT1, nextT2, nextVarStruct))
					ss.add(s2.apply(s1));
			}
		}
		return ss;
	}
	private UConstraint pickUnsolved(UConstraintSet cset) {
		// Constraint sets are ordered by a huristic that tries
		// to put simple constraints first (see UConstraint.complexity())
		for (UConstraint constraint : cset)
			if (!constraint.solved())
				return constraint;
		throw new IllegalStateException();
	}
	public Set<UConstraintSet> rfactor(UConstraint constraint) {
		Type T1 = constraint.T1, T2 = constraint.T2;
		if (T1 instanceof Type.Function && T2 instanceof Type.Function) {
			Type.Function T1f = (Type.Function)T1, T2f = (Type.Function)T2;
			UConstraintSet cset = new UConstraintSet(
				new UConstraint(T2f.parameterType, T1f.parameterType),
				new UConstraint(T1f.returnType, T2f.returnType)
				);
			return rfactor(cset);
		}
		else if (T1 instanceof Type.Intersection && T2 instanceof Type.Intersection) {
			Type.Intersection T1i = (Type.Intersection)T1, T2i = (Type.Intersection)T2;
			UConstraintSet cset = new UConstraintSet(
				new UConstraint(T1i.left, T2i.left),
				new UConstraint(T1i.right, T2i.right)
				);
			return rfactor(cset);
		}
		else if (T1 instanceof Type.Intersection || T2 instanceof Type.Intersection) {
			if (T1 instanceof Type.EvarApplication || T2 instanceof Type.EvarApplication)
				return Util.set(new UConstraintSet(constraint));
			Type.Intersection Ti;
			Type T;
			UConstraintSet cset1, cset2;
			if (T1 instanceof Type.Intersection) {
				Ti = (Type.Intersection)T1; T = T2;
				cset1 = new UConstraintSet(new UConstraint(Ti.left, T),
						new UConstraint(Ti.right, new Type.Omega()));
				cset2 = new UConstraintSet(new UConstraint(Ti.left, new Type.Omega()),
						new UConstraint(Ti.right, T));
			}
			else {
				Ti = (Type.Intersection)T2; T = T1;
				cset1 = new UConstraintSet(new UConstraint(T, Ti.left),
						new UConstraint(new Type.Omega(), Ti.right));
				cset2 = new UConstraintSet(new UConstraint(new Type.Omega(), Ti.left),
						new UConstraint(T, Ti.right));
			}
			Set<UConstraintSet> sset = new HashSet<UConstraintSet>();
			if (T.isOmega()) {
				UConstraintSet cset = new UConstraintSet(
						new UConstraint(Ti.left, new Type.Omega()),
						new UConstraint(Ti.right, new Type.Omega())
						);
				return rfactor(cset);
			}
			if (T instanceof Type.Function) {
				Type.Function F = (Type.Function)T;
				Type param = UConstraint.estrip(F.parameterType);
				// If this is a record field type,
				if (param instanceof Type.Label || param instanceof Type.SimpleVariable) {
					// For efficiency, we look ahead and only add to sset those paths that
					// have a chance of being unified.
					// Only add cset1 if there is a chance it can be unified
					if (hasMatchingField(Ti.left, param))
						sset.addAll(rfactor(cset1));
					// Only add cset2 if there is a chance it can be unified
					if (hasMatchingField(Ti.right, param))
						sset.addAll(rfactor(cset2));
				}
			}
			// If the above optimisation didn't apply, we just factor
			// the normal way...
			if (sset.size() == 0)
				sset = Util.sset(rfactor(cset1), rfactor(cset2));
			return sset;
		}
		else if (T1 instanceof Type.EvarApplication || T2 instanceof Type.EvarApplication) {
			if (T1 instanceof Type.EvarApplication && T2.isOmega())
				T2 = new Type.EvarApplication(((Type.EvarApplication)T1).e, new Type.Omega());
			else if (T2 instanceof Type.EvarApplication && T1.isOmega())
				T1 = new Type.EvarApplication(((Type.EvarApplication)T2).e, new Type.Omega());

			if (T1 instanceof Type.EvarApplication && T2 instanceof Type.EvarApplication
			   && ((Type.EvarApplication)T1).e.equals(((Type.EvarApplication)T2).e)) {
				Type.EvarApplication e_T1 = (Type.EvarApplication)T1;
				Type.EvarApplication e_T2 = (Type.EvarApplication)T2;
				Expansion.Variable e = e_T1.e;
				UConstraintSet orig_cset = new UConstraintSet(new UConstraint(e_T1.K, e_T2.K));
				Set<UConstraintSet> csets = new HashSet<UConstraintSet>();
				for (UConstraintSet int_cset : rfactor(orig_cset)) {
					UConstraintSet cset = new UConstraintSet();
					for (UConstraint int_constraint : int_cset) {
						UConstraint wrapped = new UConstraint(e, int_constraint);
						cset.add(wrapped);
					}
					csets.add(cset);
				}
				return csets;
			}
			else {
				return Util.set(new UConstraintSet(constraint));
			}
		}
		else {
			return Util.set(new UConstraintSet(constraint));
		}
	}
	public Set<UConstraintSet> rfactor(UConstraintSet cset) {
		List<Set<UConstraintSet>> a = new ArrayList<Set<UConstraintSet>>();
		for (UConstraint constraint : cset)
			a.add(rfactor(constraint));
		return combinate(a, 0);
	}
	private Set<UConstraintSet> combinate(List<Set<UConstraintSet>> a, int start) {
		if (start == a.size() - 1) return a.get(start);
		Set<UConstraintSet> result = new HashSet<UConstraintSet>();
		for (UConstraintSet head_cset : a.get(start))
			for (UConstraintSet remainder_cset : combinate(a, start + 1))
				result.add(new UConstraintSet(head_cset, remainder_cset));
		return result;
	}
	/** Return empty set if constraint cannot be solved */
	public Set<Substitution> opus(UConstraint constraint, VarStruct varStruct) {
		if (constraint.solved())
			throw new IllegalStateException("opus was given a solved constraint " + constraint);
		Type T1 = constraint.T1, T2 = constraint.T2;
		Set<Substitution> ss = new HashSet<Substitution>();
		if (T1 instanceof Type.SimpleVariable && T2 instanceof Type.Simple
				|| T2 instanceof Type.SimpleVariable && T1 instanceof Type.Simple) {
			Type.SimpleVariable H;
			Type T;
			if (T1 instanceof Type.SimpleVariable)	{ H = (Type.SimpleVariable)T1; T = T2; }
			else							{ H = (Type.SimpleVariable)T2; T = T1; }
			opusT(varStruct, ss, H, T);
		}
		else if (T1 instanceof Type.EvarApplication || T2 instanceof Type.EvarApplication) {
			Type.EvarApplication e_T;
			Type T;
			if (T1 instanceof Type.EvarApplication) { e_T = (Type.EvarApplication)T1; T = T2; }
			else									{ e_T = (Type.EvarApplication)T2; T = T1; }
			if (T instanceof Type.Simple)			   opusES(varStruct, ss, e_T, (Type.Simple)T);
			else if (T instanceof Type.Intersection)	opusEI(varStruct, ss, e_T, (Type.Intersection)T);
			else if (T instanceof Type.EvarApplication) {
				Type.EvarApplication e1_T1 = e_T, e2_T2 = (Type.EvarApplication)T;
				Expansion.Variable e1 = e1_T1.e, e2 = e2_T2.e;
				if (e1.equals(e2))  opusE(varStruct, ss, e1_T1, e2_T2);
				else				opusEE(varStruct, ss, e1_T1, e2_T2);
			}
		}
		return ss;
	}
	protected void opusT(VarStruct varStruct, Set<Substitution> ss,
			Type.SimpleVariable H, Type T) {
		if (T instanceof Type.SimpleVariable)	opusTT(varStruct, ss, H, (Type.SimpleVariable)T);
		else if (T instanceof Type.Constant)	 opusTC(varStruct, ss, H, (Type.Constant)T);
		else if (T instanceof Type.Function)	 opusTA(varStruct, ss, H, (Type.Function)T);
	}
	protected void opusTT(VarStruct varStruct, Set<Substitution> ss,
			Type.SimpleVariable H1, Type.SimpleVariable H2) {
		LConstraint labels = new LConstraint(H1.constraint);
		labels.addAll(H2.constraint);
		Type.SimpleVariable HN = new Type.SimpleVariable(labels);
		ss.add(new Substitution().with(H1, HN).with(H2, HN));
	}
	protected void opusTC(VarStruct varStruct, Set<Substitution> ss,
			Type.SimpleVariable H, Type.Constant C) {
		if (C.meets(H.constraint))
			ss.add(new Substitution().with(H, C));
	}
	protected void opusTA(VarStruct varStruct, Set<Substitution> ss,
			Type.SimpleVariable H, Type.Function F) {
		if (!F.ovars().contains(H))
			ss.add(new Substitution().with(H, F));
	}
	protected void opusES(VarStruct varStruct, Set<Substitution> ss,
			Type.EvarApplication e_T, Type.Simple S) {
		ss.add(new Substitution().with(e_T.e,
					Substitution.orenaming(varStruct.get(e_T.e).ovars())));
	}
	protected void opusE(VarStruct varStruct, Set<Substitution> ss,
			Type.EvarApplication e_T1, Type.EvarApplication e_T2) {
		Type _T1 = e_T1.K, _T2 = e_T2.K;
		Expansion.Variable e = e_T1.e;
		Set<Substitution> inner_ss = opus(new UConstraint(_T1, _T2), varStruct.get(e));
		if (inner_ss.size() > 0)
			ss.add(new Substitution().with(e, isect(inner_ss)));
		else
			ss.add(new Substitution().with(e,
						new Expansion.EvarApplication(e, new Expansion.Omega())));
	}
	protected Expansion isect(Set<Substitution> ss) {
		Set<Substitution> tail = new HashSet<Substitution>(ss);
		Iterator<Substitution> it = tail.iterator();
		Substitution head = null;
		if (it.hasNext()) {
			head = it.next();
			it.remove();
		}
		Expansion.Variable e = new Expansion.Variable();
		switch (ss.size()) {
			case 0: return new Expansion.EvarApplication(e, new Expansion.Omega());
			case 1: return new Expansion.EvarApplication(e, head);
			default: return new Expansion.Intersection(new Expansion.EvarApplication(e, head),
																														                isect(tail));
		}
	}
	// Tests whether a record type contains a field that can be matched with a
	// particular label type (either a label or a constrained simple type variable).
	private boolean hasMatchingField(Type recordType, Type labelType) {
		if (recordType instanceof Type.Function) {
			Type.Function F = (Type.Function)recordType;
			return _U(new UConstraintSet(new UConstraint(F.parameterType, labelType))).size() > 0;
		}
		else if (recordType instanceof Type.Intersection) {
			Type.Intersection Ti = (Type.Intersection)recordType;
			return hasMatchingField(Ti.left, labelType) || hasMatchingField(Ti.right, labelType);
		}
		else if (recordType instanceof Type.EvarApplication) {
			Type.EvarApplication e_T = (Type.EvarApplication)recordType;
			return hasMatchingField(e_T.K, labelType);
		}
		else
			return false;
	}
	protected void opusEI(VarStruct varStruct, Set<Substitution> ss,
			Type.EvarApplication e_T, Type.Intersection Ti) {
		Expansion.Variable e1 = new Expansion.Variable();
		Expansion.Variable e2 = new Expansion.Variable();
		if (originalEI) { // use the original opusEI rule
			ss.add(new Substitution().with(e_T.e, Substitution.orenaming(varStruct.get(e_T.e).ovars())));
		}
		ss.add(new Substitution().with(e_T.e, new Expansion.Intersection(
														new Expansion.EvarApplication(e1, new Substitution()),
														new Expansion.EvarApplication(e2, new Substitution())
										)));
	}
	protected void opusEE(VarStruct varStruct, Set<Substitution> ss,
			Type.EvarApplication e1_T1, Type.EvarApplication e2_T2) {
		if (originalEE) { // use the original opusEE rule
			Expansion.Variable e1 = e1_T1.e;
			Type _T1 = e1_T1.K;
			Expansion.Variable e2 = e2_T2.e;
			Type _T2 = e2_T2.K;

			Expansion.Variable e3 = new Expansion.Variable();
			Expansion.Variable e4 = new Expansion.Variable();
			Expansion.Variable e5 = new Expansion.Variable();
			UConstraint dleft = new UConstraint(new Type.EvarApplication(e3, _T1), _T2);
			UConstraint dright = new UConstraint(_T1, new Type.EvarApplication(e4, _T2));
			VarStruct leftVarStruct = new VarStruct();
			leftVarStruct.put(e3, varStruct.get(e1), false);
			leftVarStruct = leftVarStruct.join(varStruct.get(e2));
			VarStruct rightVarStruct = new VarStruct();
			rightVarStruct.put(e4, varStruct.get(e2), false);
			rightVarStruct = rightVarStruct.join(varStruct.get(e1));
			Set<Set<Substitution>> leftPowerset = new HashSet<Set<Substitution>>();
			Set<Set<Substitution>> rightPowerset = new HashSet<Set<Substitution>>();
			leftPowerset.add(opus(dleft, leftVarStruct));
			rightPowerset.add(opus(dright, rightVarStruct));
			for (Set<Substitution> leftss : leftPowerset) {
				for (Set<Substitution> rightss : rightPowerset) {
					Expansion Eleft = isect(leftss);
					Expansion Eright = isect(rightss);
					Expansion E1 = Eleft.apply((Expansion)new Expansion.EvarApplication(e3, new Substitution()));
					Expansion E2 = Eright.restrictedTo(_T1.varStruct());
					Expansion E3 = Eleft.restrictedTo(_T2.varStruct());
					Expansion E4 = Eright.apply((Expansion)new Expansion.EvarApplication(e4, new Substitution()));
					ss.add(new Substitution().with(
								e1, new Expansion.EvarApplication(e5, new Expansion.Intersection(E1, E2))).with(
								e2, new Expansion.EvarApplication(e5, new Expansion.Intersection(E3, E4)))
							);
				}
			}
		}
		else {
			Expansion.EvarApplication gid = new Expansion.EvarApplication(
					new Expansion.Variable(), new Substitution());
			if (e1_T1.K instanceof Type.SimpleVariable || !(e2_T2.K instanceof Type.Simple))
				ss.add(new Substitution().with(e1_T1.e, new Expansion.EvarApplication(e2_T2.e, gid)));
			else
				ss.add(new Substitution().with(e2_T2.e, new Expansion.EvarApplication(e1_T1.e, gid)));
		}
	}
	private boolean isFunctionTypeVariable(Type.EvarApplication e_T) {
		Type T = e_T.K;
		return T instanceof Type.Function;
	}
	private boolean isRecordConstraint(Type.EvarApplication e_T) {
		Expansion.Variable e = e_T.e;
		Type T = e_T.K;
		if (T instanceof Type.Function) {
			Type.Function F = (Type.Function)T;
			Type T1 = F.parameterType;
			if (T1 instanceof Type.SimpleVariable) {
				Type.SimpleVariable var = (Type.SimpleVariable)T1;
				if (var.constraint.size() > 0)
					return true;
			}
		}
		return false;
	}
}
