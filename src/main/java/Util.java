import java.util.*;

public class Util {
	public static String brac(Object o, boolean yes) { return yes ? "(" + o + ")" : o.toString(); }

	public static <X> Set<X> union(Set<X> set1, Set<X> set2) {
		HashSet<X> set = new HashSet<>();
		set.addAll(set1);
		set.addAll(set2);
		return set;
	}

	public static <X> Set<X> set(X... xs) {
		Set<X> set = new HashSet<>();
		for (X x : xs)
			set.add(x);
		return set;
	}

	public static <X> Set<X> sset(Set<X>... sets) {
		Set<X> all = new HashSet<>();
		for (Set<X> set : sets)
			all.addAll(set);
		return all;
	}

	public static <X> Set<Set<X>> powerset(Set<X> originalSet) {
		Set<Set<X>> sets = new HashSet<>();
		if (originalSet.isEmpty()) {
			sets.add(Collections.emptySet());
			return sets;
		}
		List<X> list = new ArrayList<>(originalSet);
		X head = list.get(0);
		Set<X> rest = new HashSet<>(list.subList(1, list.size()));
		for (Set<X> set : powerset(rest)) {
			Set<X> newSet = new HashSet<>();
			newSet.add(head);
			newSet.addAll(set);
			sets.add(newSet);
			sets.add(set);
		}
		return sets;
	}

	public static String spaces(int n) {
		String s = "";
		for (int i = 0; i < n; i++)
			s += " ";
		return s;
	}
}
