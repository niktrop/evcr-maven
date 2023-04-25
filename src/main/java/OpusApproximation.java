import java.util.*;

/** This modifies OpusBeta to use the original opusEI rule from Opus */
public class OpusApproximation extends OpusBeta {
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
}
