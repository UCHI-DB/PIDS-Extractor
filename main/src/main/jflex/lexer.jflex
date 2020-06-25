/**
 * Lexer for Pattern Extractor
 */
package edu.uchicago.cs.db.subattr.extract.parser;

import java_cup.runtime.*;
%%
%class Lexer
%unicode
%cupsym Sym
%function scan
%type Token

LineTerminator = \r|\n|\r\n
Whitespace     = ({LineTerminator} | [ \t\f])+

IntLiteral=[0-9]+
WordLiteral=[a-zA-Z]+

%%

<YYINITIAL> {
{IntLiteral}        {return new TInt(yytext());}
{WordLiteral}       {return new TWord(yytext());}
{Whitespace}        {return new TSpace();}
"-"                 {return new TSymbol(yytext());}
"_"                 {return new TSymbol(yytext());}
"("                 {return new TSymbol(yytext());}
")"                 {return new TSymbol(yytext());}
"["                 {return new TSymbol(yytext());}
"]"                 {return new TSymbol(yytext());}
"{"                 {return new TSymbol(yytext());}
"}"                 {return new TSymbol(yytext());}
","                 {return new TSymbol(yytext());}
"."                 {return new TSymbol(yytext());}
":"                 {return new TSymbol(yytext());}
";"                 {return new TSymbol(yytext());}
"/"                 {return new TSymbol(yytext());}
"\\"                 {return new TSymbol(yytext());}
}
.                   {return new TSymbol(yytext());}

