public class NameSequence {
	private static String[] names = new String[10000];
	static {
		char[] chars = {'a','a','a','a','a'};
		int digits = 1;
		for (int i = 0; i < names.length; i++) {
			int n = i;
			boolean max = true;
			for (int p = digits - 1; p >= 0; p--) {
				chars[p] = (char)('a' + n % 26);
				if (chars[p] != 'z')
					max = false;
				n /= 26;
			}
			names[i] = new String(chars, 0, digits);
			if (max) digits++;
		}
	}
	private int counter = 0;
	public NameSequence() {}
	public void reset() { counter = 0; }
	public String next() {
		String name = names[counter%names.length];
		int suffix = counter/names.length;
		if (suffix > 0)
			name += suffix;
		counter++;
		return name;
	}
}
