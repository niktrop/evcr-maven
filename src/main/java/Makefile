all: SysevcrParser.class Util.class Term.class Item.class Type.class Expansion.class Substitution.class Typing.class TermContext.class UConstraint.class UConstraintSet.class Unifier.class OpusBeta.class LConstraint.class VarStruct.class Shell.class

%.class: %.java
	javac -cp java-cup-11a-runtime.jar *.java

SysevcrParser.java: sysevcr.cup SysevcrLexer.java
	javacup -parser SysevcrParser -symbols SysevcrSymbols sysevcr.cup 

SysevcrLexer.java: sysevcr.flex
	jflex sysevcr.flex
