import java.util.*;

public interface Unifier {
	Set<Substitution> U(UConstraintSet constraints);
	Set<Substitution> U(UConstraintSet constraints, VarStruct vs);
}
