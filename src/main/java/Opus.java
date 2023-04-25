import java.util.*;

/**
 * Most of OpusBeta has been implemented in a way that is compatible with Opus.
 * We only need to override the implementation of EI-unify and EE-unify
 */
public class Opus extends OpusBeta {
	protected void opusEI(VarStruct varStruct, Set<Substitution> ss,
			Type.EvarApplication e_T, Type.Intersection Ti) {
		Expansion.Variable e1 = new Expansion.Variable();
		Expansion.Variable e2 = new Expansion.Variable();
		ss.add(new Substitution().with(e_T.e, Substitution.orenaming(varStruct.get(e_T.e).ovars())));
		ss.add(new Substitution().with(e_T.e, new Expansion.Intersection(
														new Expansion.EvarApplication(e1, new Substitution()),
														new Expansion.EvarApplication(e2, new Substitution())
										)));
	}

	protected void opusEE(VarStruct varStruct, Set<Substitution> ss,
			Type.EvarApplication e1_T1, Type.EvarApplication e2_T2) {
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
		for (Set<Substitution> leftss : Util.powerset(opus(dleft, leftVarStruct))) {
			for (Set<Substitution> rightss : Util.powerset(opus(dright, rightVarStruct))) {
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
}
