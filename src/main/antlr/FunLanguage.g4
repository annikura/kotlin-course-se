grammar FunLanguage;

file
    : block EOF
    ;
block
    : statement*
    ;
blockWithBraces
    : '{' block '}'
    ;
statement
    : func=function
    | var=variableDeclaration
    | exp=expression
    | loop=whileStatement
    | branch=ifStatement
    | assign=assignment
    | ret=returnStatement
    ;
function
    : 'fun' id=IDENTIFIER '(' params=parameters ')' funBlock=blockWithBraces
    ;
parameters
    : (IDENTIFIER (',' IDENTIFIER)*)?
    ;
variableDeclaration
    : 'var' id=IDENTIFIER ('=' exp=expression)?
    ;
whileStatement
    : 'while' '(' cond=expression ')' whileBlock=blockWithBraces
    ;
ifStatement
    : 'if' '(' cond=expression ')' ifBlock=blockWithBraces ('else' elseBlock=blockWithBraces)?
    ;
assignment
    : id=IDENTIFIER '=' exp=expression
    ;
returnStatement
    : 'return' exp=expression
    ;
arguments
    : (expression (',' expression)*)?
    ;
functionCall
    : id=IDENTIFIER '(' args=arguments ')'
    ;

/*
    Арифметическое выражение с операциями: +, -, *, /, %, >, <, >=, <=, ==, !=, ||, &&
    Семантика и приоритеты операций примерно как в Си
*/
expression
    : num=LITERAL
    | func=functionCall
    | id=IDENTIFIER
    | '(' exp=expression ')'
    | <assoc=left> firstExp=expression (op=MUL | op=DIV | op=MOD) secondExp=expression
    | <assoc=left> firstExp=expression (op=PLUS | op=MINUS) secondExp=expression
    | <assoc=left> firstExp=expression (op=LESS_THAN | op=GRATER_THAN | op=LE | op=GE) secondExp=expression
    | <assoc=left> firstExp=expression (op=EQ | op=NEQ) secondExp=expression
    | <assoc=left> firstExp=expression (op=AND) secondExp=expression
    | <assoc=left> firstExp=expression (op=OR) secondExp=expression
    ;

// precedence = 1
MUL : '*';
DIV : '/';
MOD : '%';
// precedence = 2
PLUS : '+';
MINUS : '-';
// precedence = 3
LESS_THAN : '<';
GRATER_THAN : '>';
LE : '<=';
GE : '>=';
// precedence = 4
EQ : '==';
NEQ : '!=';
// precedence = 5
AND : '&&';
// precedence = 6
OR : '||';

/* Идентификатор как в Си */
IDENTIFIER
    : [a-zA-Z_][a-zA-Z_0-9]*
    ;
/* Десятичный целочисленный литерал без ведущих нулей */
LITERAL
    : '0'
    | [1-9][0-9]*
    ;
WS
    : ([ \t\u000C\r\n]+ | ('\\' [^\n]*)) -> skip
    ;