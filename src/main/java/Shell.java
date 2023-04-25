import java.io.*;
import java.util.*;

/**
 * The main program.
 *
 * Options:
 *
 * s - show evaluation steps
 * n - don't do type inference
 * 12 - use opus
 * */
public class Shell {
	private static boolean noTypes = false;
	public static boolean showEval = false;
	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			if (args[0].contains("s")) showEval = true;
			if (args[0].contains("n")) noTypes = true;
			if (args[0].contains("1")) OpusBeta.originalEI = true;
			if (args[0].contains("2")) OpusBeta.originalEE = true;
		}
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		String line;
		String program = "";
		System.out.print("|o o| ");
		while ((line = in.readLine()) != null) {
			if (line.equals("#noTypes")) {
				noTypes = !noTypes;
				continue;
			}
			program += line + "\n";
			if (line.matches(".*;;")) {
				try {
					SysevcrLexer lexer = new SysevcrLexer(new StringReader(program));
					SysevcrParser parser = new SysevcrParser(lexer);
					Object ast = parser.parse().value;
					if (ast instanceof Term) {
						Term t = (Term)ast;
						Set<Typing> typs = noTypes ? null : Typing.renameZeroSet(t.I());
						if (typs != null && typs.size() == 0)
							System.out.println(": No typings found");
						else {
							if (typs != null)
								System.out.print(Typing.typingsStr(typs));
							if (t.fv().size() == 0 && !(t instanceof Term.Value)
									|| !t.toString().equals(ast.toString())) {
								System.out.println("= " + t);
								Term result = t.evalTop();
							}
							else System.out.println("Free variables: " + t.fv());
						}
					}
					System.out.println();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					program = "";
					System.out.print("|o o| ");
				}
			}
		}
	}
}
