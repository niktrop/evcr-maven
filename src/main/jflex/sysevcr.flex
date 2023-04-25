import java_cup.runtime.*;
      
%%

%class SysevcrLexer
%line
%column
%cupsym SysevcrSymbols
%cup
%{   
    StringBuffer string = new StringBuffer();

    private Symbol symbol(int type) {
        return new Symbol(type, yyline, yycolumn);
    }
    
    private Symbol symbol(int type, Object value) {
        return new Symbol(type, yyline, yycolumn, value);
    }
%}

LineTerminator = \r|\n|\r\n
WhiteSpace     = {LineTerminator} | [ \t\f]
Integer        = 0 | -?[1-9][0-9]*
Identifier     = [$A-Za-z_][$A-Za-z_0-9]*
Comment        = {HashComment} | {SlashSlashComment}

HashComment       = #[^\r\n]*
SlashSlashComment = "//"[^\r\n]*



%state STRING
   
%%

<YYINITIAL> {

 {Comment}       { /* ignore */ }
 {Integer}       { return symbol(SysevcrSymbols.INT, new Integer(yytext())); }
 "\\"             { return symbol(SysevcrSymbols.LAMBDA); }
 "->"            { return symbol(SysevcrSymbols.ARROW); }
 "^"             { return symbol(SysevcrSymbols.WEDGE); }
 "."             { return symbol(SysevcrSymbols.DOT); }
 "+"             { return symbol(SysevcrSymbols.ADD_SYM); }
 "-"             { return symbol(SysevcrSymbols.SUB_SYM); }
 "*"             { return symbol(SysevcrSymbols.MUL_SYM); }
 "/"             { return symbol(SysevcrSymbols.DIV_SYM); }
 "&&"            { return symbol(SysevcrSymbols.AND_SYM); }
 "||"            { return symbol(SysevcrSymbols.OR_SYM); }
 "<"             { return symbol(SysevcrSymbols.LT_SYM); }
 ">"             { return symbol(SysevcrSymbols.GT_SYM); }
 "<="            { return symbol(SysevcrSymbols.LE_SYM); }
 ">="            { return symbol(SysevcrSymbols.GE_SYM); }
 "=="            { return symbol(SysevcrSymbols.EQ_SYM); }
 "++"            { return symbol(SysevcrSymbols.CAT_SYM); }
 "="             { return symbol(SysevcrSymbols.EQUAL); }
 ";"             { return symbol(SysevcrSymbols.SEMI); }
 ";;"            { return symbol(SysevcrSymbols.SEMISEMI); }
 ","             { return symbol(SysevcrSymbols.COMMA); }
 "$primif"       { return symbol(SysevcrSymbols.PRIMIF); }
 "if"            { return symbol(SysevcrSymbols.IF); }
 "else"          { return symbol(SysevcrSymbols.ELSE); }
 "cat"           { return symbol(SysevcrSymbols.CAT); }
 "str"           { return symbol(SysevcrSymbols.STR); }
 "let"           { return symbol(SysevcrSymbols.LET); }
 "in"            { return symbol(SysevcrSymbols.IN); }
 "_add"          { return symbol(SysevcrSymbols.ADD); }
 "_sub"          { return symbol(SysevcrSymbols.SUB); }
 "_mul"          { return symbol(SysevcrSymbols.MUL); }
 "_div"          { return symbol(SysevcrSymbols.DIV); }
 "_and"          { return symbol(SysevcrSymbols.AND); }
 "_or"           { return symbol(SysevcrSymbols.OR); }
 "not"           { return symbol(SysevcrSymbols.NOT); }
 "_lt"           { return symbol(SysevcrSymbols.LT); }
 "_gt"           { return symbol(SysevcrSymbols.GT); }
 "_le"           { return symbol(SysevcrSymbols.LE); }
 "_ge"           { return symbol(SysevcrSymbols.GE); }
 "_eq"           { return symbol(SysevcrSymbols.EQ); }
 "$println"      { return symbol(SysevcrSymbols.PRINTLN); }
 "?"             { return symbol(SysevcrSymbols.QMARK); }
 "("             { return symbol(SysevcrSymbols.LROUND); }
 ")"             { return symbol(SysevcrSymbols.RROUND); }
 "["             { return symbol(SysevcrSymbols.LSQUARE); }
 "]"             { return symbol(SysevcrSymbols.RSQUARE); }
 "{"             { return symbol(SysevcrSymbols.LCURLY); }
 "}"             { return symbol(SysevcrSymbols.RCURLY); }
 "true"          { return symbol(SysevcrSymbols.BOOL, new Boolean(true)); }
 "false"         { return symbol(SysevcrSymbols.BOOL, new Boolean(false)); }
 "()"            { return symbol(SysevcrSymbols.VOID); }
 "{}"            { return symbol(SysevcrSymbols.EMPTYREC); }
 {Identifier}    { return symbol(SysevcrSymbols.IDENT, yytext());}
 ":"             { return symbol(SysevcrSymbols.COLON); }
 \"              { string.setLength(0); yybegin(STRING); }
 {WhiteSpace}    {}   
}

<STRING> {
 \"            { yybegin(YYINITIAL); return symbol(SysevcrSymbols.STRING, string.toString()); }
 [^\n\r\"\\]+  { string.append( yytext() ); }
 \\t           { string.append('\t'); }
 \\n           { string.append('\n'); }

 \\r           { string.append('\r'); }
 \\\"          { string.append('\"'); }
 \\            { string.append('\\'); }
}

[^]                     { throw new Error("Illegal character <"+yytext()+">"); }
