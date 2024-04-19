// Grammaire du langage PROJET
// CMPL L3info 
// Nathalie Girard, Veronique Masson, Laurent Perraudeau
// il convient d'y inserer les appels a {PtGen.pt(k);}
// relancer Antlr apres chaque modification et raffraichir le projet Eclipse le cas echeant

// attention l'analyse est poursuivie apres erreur si l'on supprime la clause rulecatch

grammar projet;

options {
  language=Java; k=1;
 }

@header {           
import java.io.IOException;
import java.io.DataInputStream;
import java.io.FileInputStream;
} 


// partie syntaxique :  description de la grammaire //
// les non-terminaux doivent commencer par une minuscule


@members {

 
// variables globales et methodes utiles a placer ici
  
}
// la directive rulecatch permet d'interrompre l'analyse a la premiere erreur de syntaxe
@rulecatch {
catch (RecognitionException e) {reportError (e) ; throw e ; }}


unite  :   unitprog {PtGen.pt(255);} EOF
      |    unitmodule  EOF
  ;
  
unitprog
  : 'programme'{PtGen.pt(0);} ident ':'  
     declarations
     corps {PtGen.pt(254);} { System.out.println("succes, arret de la compilation "); }
  ;
  
unitmodule
  : 'module' ident ':' 
     declarations   
  ;
  
declarations
  : partiedef? partieref? consts? vars? decprocs? 
  ;
  
partiedef
  : 'def' ident  (',' ident )* ptvg
  ;
  
partieref: 'ref'  specif (',' specif)* ptvg
  ;
  
specif  : ident  ( 'fixe' '(' type  ( ',' type  )* ')' )? 
                 ( 'mod'  '(' type  ( ',' type  )* ')' )? 
  ;
  
consts  : 'const' ( ident  '=' valeur {PtGen.pt(7);} ptvg )+
  ;
  
vars  : 'var' ( type ident {PtGen.pt(8);} ( ','  ident {PtGen.pt(8);} )* ptvg  )+ {PtGen.pt(9);}
  ;
  
type  : 'ent'  {PtGen.pt(5);}
  |     'bool' {PtGen.pt(6);}
  ;
  
decprocs: {PtGen.pt(40);} (decproc ptvg)+ {PtGen.pt(46);}
  ;
  
decproc : 'proc' ident {PtGen.pt(41);} parfixe? parmod? {PtGen.pt(44);} consts? vars? corps {PtGen.pt(45);} 
  ;
  
ptvg  : ';'
  | 
  ;
  
corps : 'debut' instructions 'fin' 
  ;
  
parfixe: 'fixe' '(' pf ( ';' pf)* ')'
  ;
  
pf  : type ident {PtGen.pt(42);} ( ',' ident {PtGen.pt(42);} )*  
  ;

parmod  : 'mod' '(' pm ( ';' pm)* ')'
  ;
  
pm  : type ident {PtGen.pt(43);} ( ',' ident {PtGen.pt(43);} )*
  ;
  
instructions
  : instruction ( ';' instruction)*
  ;
  
instruction
  : inssi
  | inscond
  | boucle
  | lecture
  | ecriture
  | affouappel
  |
  ;
  
inssi : 'si' expression {PtGen.pt(30);} 'alors' {} instructions ('sinon' {PtGen.pt(31);} instructions)? 'fsi' {PtGen.pt(32);}
  ;
  
inscond : 'cond' {PtGen.pt(35);}  expression {PtGen.pt(36);} ':' instructions
          (',' {PtGen.pt(37);}  expression {PtGen.pt(36);} ':' instructions)* 
          ('aut' {PtGen.pt(38);} instructions |  ) 
          'fcond' {PtGen.pt(39);}
  ;
  
boucle  : 'ttq' {PtGen.pt(33);} expression {PtGen.pt(30);} 'faire' instructions 'fait' {PtGen.pt(34);}
  ;
  
lecture: 'lire' '(' ident {PtGen.pt(28);} ( ',' ident {PtGen.pt(28);} )* ')' 
  ;
  
ecriture: 'ecrire' '(' expression {PtGen.pt(29);} ( ',' expression {PtGen.pt(29);} )* ')' 
   ;
  
affouappel
  : ident {PtGen.pt(10);} (    ':='  expression {PtGen.pt(11);}
            | (effixes (effmods)?)? {PtGen.pt(49);}
           )
  ;

effixes : '(' (expression {PtGen.pt(47);} (',' expression {PtGen.pt(47);} )*)? ')'
  ;

effmods :'(' (ident {PtGen.pt(48);} (',' ident {PtGen.pt(48);} )*)? ')'
  ;

expression: (exp1 ) ({PtGen.pt(14);} 'ou' exp1 {PtGen.pt(14);} {PtGen.pt(19);})*
  ;

exp1  : exp2  ({PtGen.pt(14);} 'et' exp2 {PtGen.pt(14);} {PtGen.pt(20);})*
  ;

exp2  : 'non' exp2 {PtGen.pt(14);} {PtGen.pt(21);}
  | exp3
  ;

exp3  : exp4
  ( '=' {PtGen.pt(13);} exp4 {PtGen.pt(13);} {PtGen.pt(22);}
  | '<>' {PtGen.pt(13);} exp4 {PtGen.pt(13);} {PtGen.pt(23);}
  | '>'  {PtGen.pt(13);} exp4 {PtGen.pt(13);} {PtGen.pt(24);}
  | '>=' {PtGen.pt(13);} exp4 {PtGen.pt(13);} {PtGen.pt(25);}
  | '<'  {PtGen.pt(13);} exp4 {PtGen.pt(13);} {PtGen.pt(26);}
  | '<=' {PtGen.pt(13);} exp4 {PtGen.pt(13);} {PtGen.pt(27);}
  ) ?
  ;

exp4  : exp5 
        ('+' {PtGen.pt(13);} exp5 {PtGen.pt(13);} {PtGen.pt(15);}
        |'-' {PtGen.pt(13);} exp5 {PtGen.pt(13);} {PtGen.pt(16);}
        )*
  ;

exp5  : primaire
        ('*' {PtGen.pt(13);} primaire {PtGen.pt(13);} {PtGen.pt(17);} 
        |'div' {PtGen.pt(13);} primaire {PtGen.pt(13);} {PtGen.pt(18);} 
        )*
  ;

primaire: valeur {PtGen.pt(101);}
  | ident {PtGen.pt(12);}
  | '(' expression ')'
  ;

valeur  : nbentier {PtGen.pt(1);}
  | '+' nbentier {PtGen.pt(1);}
  | '-' nbentier {PtGen.pt(2);}
  | 'vrai' {PtGen.pt(3);}
  | 'faux' {PtGen.pt(4);}
  ;

// partie lexicale  : cette partie ne doit pas etre modifiee  //
// les unites lexicales de ANTLR doivent commencer par une majuscule
// Attention : ANTLR n'autorise pas certains traitements sur les unites lexicales,
// il est alors ncessaire de passer par un non-terminal intermediaire
// exemple : pour l'unit lexicale INT, le non-terminal nbentier a du etre introduit


nbentier  :   INT { UtilLex.valEnt = Integer.parseInt($INT.text);}; // mise a jour de valEnt

ident : ID  { UtilLex.traiterId($ID.text); } ; // mise a jour de numIdCourant
     // tous les identificateurs seront places dans la table des identificateurs, y compris le nom du programme ou module
     // (NB: la table des symboles n'est pas geree au niveau lexical mais au niveau du compilateur)


ID  :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;

// zone purement lexicale //

INT :   '0'..'9'+ ;
WS  :   (' '|'\t' |'\r')+ {skip();} ; // definition des "blocs d'espaces"
RC  :   ('\n') {UtilLex.incrementeLigne(); skip() ;} ; // definition d'un unique "passage a la ligne" et comptage des numeros de lignes

COMMENT
  :  '\{' (.)* '\}' {skip();}   // toute suite de caracteres entouree d'accolades est un commentaire
  |  '#' ~( '\r' | '\n' )* {skip();}  // tout ce qui suit un caractere diese sur une ligne est un commentaire
  ;

// commentaires sur plusieurs lignes
ML_COMMENT    :   '/*' (options {greedy=false;} : .)* '*/' {$channel=HIDDEN;}
    ;

