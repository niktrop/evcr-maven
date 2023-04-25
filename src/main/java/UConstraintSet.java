import java.util.*;

public class UConstraintSet extends TreeSet<UConstraint> {
	public UConstraintSet() {}
	public UConstraintSet(UConstraint... constraints) {
		for (UConstraint constraint : constraints)
			add(constraint);
	}
	public UConstraintSet(UConstraintSet... csets) {
		for (UConstraintSet cset : csets)
			for (UConstraint constraint : cset)
				add(constraint);
	}
	public UConstraintSet simplify() {
		UConstraintSet result = new UConstraintSet();
		for (UConstraint constraint : this)
			if (!constraint.solved())
				result.add(constraint);
		return result;
	}
	public boolean solved() {
		for (UConstraint constraint : this)
			if (!constraint.solved())
				return false;
		return true;
	}
}
