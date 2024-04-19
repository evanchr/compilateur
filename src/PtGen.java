/*********************************************************************************
 * VARIABLES ET METHODES FOURNIES PAR LA CLASSE UtilLex (cf libClass_Projet)     *
 *       complement à l'ANALYSEUR LEXICAL produit par ANTLR                      *
 *                                                                               *
 *                                                                               *
 *   nom du programme compile, sans suffixe : String UtilLex.nomSource           *
 *   ------------------------                                                    *
 *                                                                               *
 *   attributs lexicaux (selon items figurant dans la grammaire):                *
 *   ------------------                                                          *
 *     int UtilLex.valEnt = valeur du dernier nombre entier lu (item nbentier)   *
 *     int UtilLex.numIdCourant = code du dernier identificateur lu (item ident) *
 *                                                                               *
 *                                                                               *
 *   methodes utiles :                                                           *
 *   ---------------                                                             *
 *     void UtilLex.messErr(String m)  affichage de m et arret compilation       *
 *     String UtilLex.chaineIdent(int numId) delivre l'ident de codage numId     *
 *     void afftabSymb()  affiche la table des symboles                          *
 *********************************************************************************/


 import java.io.*;

 /**
  * classe de mise en oeuvre du compilateur
  * =======================================
  * (verifications semantiques + production du code objet)
  *
  * @author Girard, Masson, Perraudeau
  */
 
 public class PtGen {
 
 
     // constantes manipulees par le compilateur
     // ----------------------------------------
 
     private static final int
 
             // taille max de la table des symboles
             MAXSYMB = 300,
 
     // codes MAPILE :
     RESERVER = 1, EMPILER = 2, CONTENUG = 3, AFFECTERG = 4, OU = 5, ET = 6, NON = 7, INF = 8,
             INFEG = 9, SUP = 10, SUPEG = 11, EG = 12, DIFF = 13, ADD = 14, SOUS = 15, MUL = 16, DIV = 17,
             BSIFAUX = 18, BINCOND = 19, LIRENT = 20, LIREBOOL = 21, ECRENT = 22, ECRBOOL = 23,
             ARRET = 24, EMPILERADG = 25, EMPILERADL = 26, CONTENUL = 27, AFFECTERL = 28,
             APPEL = 29, RETOUR = 30,
 
     // codes des valeurs vrai/faux
     VRAI = 1, FAUX = 0,
 
     // types permis :
     ENT = 1, BOOL = 2, NEUTRE = 3,
 
     // categories possibles des identificateurs :
     CONSTANTE = 1, VARGLOBALE = 2, VARLOCALE = 3, PARAMFIXE = 4, PARAMMOD = 5, PROC = 6,
             DEF = 7, REF = 8, PRIVEE = 9,
 
     //valeurs possible du vecteur de translation 
     TRANSDON = 1, TRANSCODE = 2, REFEXT = 3;
 
 
     // utilitaires de controle de type
     // -------------------------------
 
     /**
      * verification du type entier de l'expression en cours de compilation
      * (arret de la compilation sinon)
      */
     private static void verifEnt() {
         if (tCour != ENT)
             UtilLex.messErr("expression entiere attendue");
     }
 
     /**
      * verification du type booleen de l'expression en cours de compilation
      * (arret de la compilation sinon)
      */
     private static void verifBool() {
         if (tCour != BOOL)
             UtilLex.messErr("expression booleenne attendue");
     }
 
     // pile pour gerer les chaines de reprise et les branchements en avant
     // -------------------------------------------------------------------
 
     private static TPileRep pileRep;
 
 
     // production du code objet en memoire
     // -----------------------------------
 
     private static ProgObjet po;
 
 
     // COMPILATION SEPAREE 
     // -------------------
     //
 
     /**
      * modification du vecteur de translation associe au code produit
      * + incrementation attribut nbTransExt du descripteur
      * NB: effectue uniquement si c'est une reference externe ou si on compile un module
      *
      * @param valeur : TRANSDON, TRANSCODE ou REFEXT
      */
     private static void modifVecteurTrans(int valeur) {
         if (valeur == REFEXT || desc.getUnite().equals("module")) {
             po.vecteurTrans(valeur);
             desc.incrNbTansExt();
         }
     }
 
     // descripteur associe a un programme objet (compilation separee)
     private static Descripteur desc;
 
 
     // autres variables fournies
     // -------------------------
 
     // MERCI de renseigner ici un nom pour le trinome, constitue EXCLUSIVEMENT DE LETTRES
     public static String trinome = "MONTIEGEHugoCHARRIEREvanHEGOValentin";
 
     private static int tCour;          // type de l'expression compilee
     private static int vCour;          // sert uniquement lors de la compilation d'une valeur (entiere ou boolenne)

     private static int nbVarG;         // compte le nombre de variables globales
     private static int infoVarG;
     private static int nbVarL;         // compte le nombre de variables locales
     private static int infoVarL;

     private static int ident;          // code du dernier ident rencontré
     private static int idIdent;        // adresse (it) dans tabSymb du dernier ident rencontré
     private static int typeIdent;      // type du dernier ident en attente rencontré
     
     private static int nbParamProc;    // compte le nombre de paramètre d'une procédure
     private static int nbParamAppel;   // compte le nombre de paramètres d'un appel
     private static int procActive;     // procédure active
 
     // TABLE DES SYMBOLES
     // ------------------
     //
     private static EltTabSymb[] tabSymb = new EltTabSymb[MAXSYMB + 1];
 
     // it = indice de remplissage de tabSymb
     // bc = bloc courant (=1 si le bloc courant est le programme principal)
     private static int it, bc;
 
     /**
      * utilitaire de recherche de l'ident courant (ayant pour code UtilLex.numIdCourant) dans tabSymb
      *
      * @param borneInf : recherche de l'indice it vers borneInf (=1 si recherche dans tout tabSymb)
      * @return : indice de l'ident courant (de code UtilLex.numIdCourant) dans tabSymb (O si absence)
      */
     private static int presentIdent(int borneInf) {
         int i = it;
         while (i >= borneInf && tabSymb[i].code != UtilLex.numIdCourant)
             i--;
         if (i >= borneInf)
             return i;
         else
             return 0;
     }
 
     /**
      * utilitaire de placement des caracteristiques d'un nouvel ident dans tabSymb
      *
      * @param code : UtilLex.numIdCourant de l'ident
      * @param cat  : categorie de l'ident parmi CONSTANTE, VARGLOBALE, PROC, etc.
      * @param type : ENT, BOOL ou NEUTRE
      * @param info : valeur pour une constante, ad d'exécution pour une variable, etc.
      */
     private static void placeIdent(int code, int cat, int type, int info) {
         if (it == MAXSYMB)
             UtilLex.messErr("debordement de la table des symboles");
         it = it + 1;
         tabSymb[it] = new EltTabSymb(code, cat, type, info);
     }
 
     /**
      * utilitaire d'affichage de la table des symboles
      */
     private static void afftabSymb() {
         System.out.println("       code           categorie      type    info");
         System.out.println("      |--------------|--------------|-------|----");
         for (int i = 1; i <= it; i++) {
             if (i == bc) {
                 System.out.print("bc=");
                 Ecriture.ecrireInt(i, 3);
             } else if (i == it) {
                 System.out.print("it=");
                 Ecriture.ecrireInt(i, 3);
             } else
                 Ecriture.ecrireInt(i, 6);
             if (tabSymb[i] == null)
                 System.out.println(" reference NULL");
             else
                 System.out.println(" " + tabSymb[i]);
         }
         System.out.println();
     }
 
 
     /**
      * initialisations A COMPLETER SI BESOIN
      * -------------------------------------
      */
     public static void initialisations() {
 
         // indices de gestion de la table des symboles
         it = 0;
         bc = 1;
 
         // pile des reprises pour compilation des branchements en avant
         pileRep = new TPileRep();
         // programme objet = code Mapile de l'unite en cours de compilation
         po = new ProgObjet();
         // COMPILATION SEPAREE: desripteur de l'unite en cours de compilation
         desc = new Descripteur();
 
         // initialisation necessaire aux attributs lexicaux
         UtilLex.initialisation();
 
         // initialisation du type de l'expression courante
         tCour = NEUTRE;
 
        
        // compteur de var globales
        nbVarG = 0;
        // info var globales
        infoVarG = 0;
 
        // compteur de var locales
        nbVarL = 0;
 
        // it de l'ident en cours
        idIdent = 0;
        // type de l'ident en cours
        typeIdent = NEUTRE;
 
        // init 
        nbParamProc = 0;
        procActive = 0;
     }
 
 
     /**
      * code des points de generation A COMPLETER
      * -----------------------------------------
      *
      * @param numGen : numero du point de generation a executer
      */
     public static void pt(int numGen) {
 
         switch (numGen) {
             case 0:
                 initialisations();
                 break;
 
             case 1:
                 // Lecture d'un entier
                 tCour = ENT;
                 vCour = UtilLex.valEnt;
                 break;
 
             case 2:
                 // Lecture d'un entier négatif
                 vCour = -UtilLex.valEnt;
                 tCour = ENT;
                 break;
 
             case 3:
                 // Lecture d'un true
                 tCour = BOOL;
                 vCour = VRAI;
                 break;
 
             case 4:
                 // Lecture d'un false
                 tCour = BOOL;
                 vCour = FAUX;
                 break;
 
             case 101 : 
                 // Empile la valeur
                 po.produire(EMPILER); 
                 po.produire(vCour);
                 break;
 
             case 5:
                 // Lecture de 'ent'
                 tCour = ENT;
                 break;
 
             case 6:
                 // Lecture de 'bool'
                 tCour = BOOL;
                 break;
 
             case 7:
                 // Déclaration de const
                 if (presentIdent(1) == 0) {
                     ident = UtilLex.numIdCourant;
                     if (tCour == BOOL) { // gestion des booléens
                         if (vCour == 0 || vCour == 1) {
                             // Ajout dans tabSymb
                             placeIdent(ident, CONSTANTE, BOOL, vCour);
                         } else {
                             UtilLex.messErr("Valeur du booléen incorrecte.");
                         }
                     } else { // gestion des entiers
                         // Ajouter dans tabSymb
                         placeIdent(ident, CONSTANTE, ENT, vCour);
                     }
                 } else {
                     // Erreur (déjà présent)
                     UtilLex.messErr("Constante déjà déclarée.");
                 }
                 break;
 
             case 8:
                 // Déclaration de vars
                 if (presentIdent(bc) == 0) { 
                     ident = UtilLex.numIdCourant;
                     if (tCour == BOOL) { // gestion des booléens
                         // Ajouter dans tabSymb
                         if (bc == 1) {
                             // Déclarations VARGLOBALES
                             placeIdent(ident, VARGLOBALE, BOOL, infoVarG);
                             nbVarG++; // incrémentation du nombre de VARGLOBALES
                             infoVarG++;
                         } else {
                             // Déclarations VARLOCALES (polyP35, point d'observation 4)
                             placeIdent(ident, VARLOCALE, BOOL, infoVarL);
                             nbVarL ++;
                             infoVarL++;
                         }
                     } else { // gestion des entiers
                         // Ajouter dans tabSymb
                         if (bc == 1) {
                             // Déclarations VARGLOBALES
                             placeIdent(ident, VARGLOBALE, ENT, infoVarG);
                             nbVarG++; // incrémentation du nombre de VARGLOBALES
                             infoVarG++; 
                         } else { 
                             // Déclarations VARLOCALES (polyP35, point d'observation 4)
                             placeIdent(ident, VARLOCALE, ENT, infoVarL);
                             nbVarL++; // incrémentation du nombre de VARLOCALES
                             infoVarL++;
                         }
                     }
                 } else {
                         // Erreur (déjà présent)
                         UtilLex.messErr("Variable déjà déclarée dans le bloc courant.");
                 }
                 break;
 
             case 9:
                 // mot-cle 'debut', si des varglobales sont déclarées, on reserve les emplacements dans la pile
                 if (nbVarG > 0 && bc ==1) {
                     po.produire(RESERVER);
                     po.produire(nbVarG);
                 } else if (nbVarL > 0) {
                     po.produire(RESERVER);
                     po.produire(nbVarL);
                 }
                 break;
 
             case 10:
                 // Lecture d'un ident dans le corps du programme
                 idIdent = presentIdent(1);
                 if (idIdent == 0) {
                     UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant) + " n'est pas dans la table des symboles ");
                 } else {
                     if (tabSymb[idIdent].categorie == CONSTANTE) {
                        UtilLex.messErr("Modification d'une constante.");
                     } else if (tabSymb[idIdent].categorie == PROC) {
                        procActive = idIdent;
                        nbParamAppel = 0;
                     } else {
                        typeIdent = tabSymb[idIdent].type;
                     }
                 }
 
                 break;
 
             case 11:
                 // Affectation
                 if (tCour == typeIdent) {
                     switch (tabSymb[idIdent].categorie) {
                         case VARGLOBALE:
                             po.produire(AFFECTERG);
                             po.produire(tabSymb[idIdent].info);
                             break;
                         case PARAMMOD:
                             po.produire(AFFECTERL);
                             po.produire(tabSymb[idIdent].info);
                             po.produire(1);
                             break;
                         case PARAMFIXE:
                         case VARLOCALE:
                             po.produire(AFFECTERL);
                             po.produire(tabSymb[idIdent].info);
                             po.produire(0);
                             break;
                         default:
                             UtilLex.messErr("Catégorie incompatible pour une affectation.");
                             break;
                     }
                 } else {
                     UtilLex.messErr("Erreur de type lors de l'affectation.");
                 }
                 break;
 
             case 12:
                 // Lecture d'un ident dans une expression
                 // Ajouter la verif que l'ident est du type attendu
                 int i = presentIdent(1);
                 if (i == 0) {
                     UtilLex.messErr("Cet ident n'est pas déclaré");
                 } else {
                     tCour = tabSymb[i].type;
                     switch (tabSymb[i].categorie) {
                         case CONSTANTE:
                             po.produire(EMPILER);
                             po.produire(tabSymb[i].info);
                             vCour = tabSymb[i].info;
                             break;
                         case VARGLOBALE:
                             po.produire(CONTENUG);
                             po.produire(tabSymb[i].info);
                             break;
                         case PARAMFIXE:
                             po.produire(CONTENUL);
                             po.produire(tabSymb[i].info);
                             po.produire(0);
                             break;
                         case PARAMMOD:
                             po.produire(CONTENUL);
                             po.produire(tabSymb[i].info);
                             po.produire(1);
                             break;
                         case VARLOCALE:
                             po.produire(CONTENUL);
                             po.produire(tabSymb[i].info);
                             po.produire(0);
                             break;
                         default:
                             UtilLex.messErr("Catégorie inconnue");
                             break;
                     }
                 }
                 break;
 
             case 13:
                 // verification que la derniere expression lue est un entier
                 verifEnt();
                 break;
 
             case 14:
                 // verification que la derniere expression lue est un booléen
                 verifBool();
                 break;
 
             case 15:
                 // addition
                 po.produire(ADD);
                 tCour = ENT;
                 break;
 
             case 16:
                 // soustraction
                 po.produire(SOUS);
                 tCour = ENT;
                 break;
 
             case 17:
                 // multiplication
                 po.produire(MUL);
                 tCour = ENT;
                 break;
 
             case 18:
                 // division
                 po.produire(DIV);
                 tCour = ENT;
                 break;
 
             case 19:
                 // 'ou' logique
                 po.produire(OU);
                 tCour = BOOL;
                 break;
 
             case 20:
                 // 'et' logique
                 po.produire(ET);
                 tCour = BOOL;
                 break;
 
             case 21:
                 // 'non' logique
                 po.produire(NON);
                 tCour = BOOL;
                 break;
 
             case 22:
                 // test d'égalité (=)
                 po.produire(EG);
                 tCour = BOOL;
                 break;
 
             case 23:
                 // test de différence (<>)
                 po.produire(DIFF);
                 tCour = BOOL;
                 break;
 
             case 24:
                 // test de stricte supériorité (>)
                 po.produire(SUP);
                 tCour = BOOL;
                 break;
 
             case 25:
                 // test de supériorité (>=)
                 po.produire(SUPEG);
                 tCour = BOOL;
                 break;
 
             case 26:
                 // test de stricte infériorité (<)
                 po.produire(INF);
                 tCour = BOOL;
                 break;
 
             case 27:
                 // test d'infériorité (<=)
                 po.produire(INFEG);
                 tCour = BOOL;
                 break;
 
             case 28:
                 // 'lire', lecture au clavier
                 idIdent = presentIdent(1);
                 if (idIdent == 0) {
                     UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant) + " n'est pas dans la table des symboles ");
                 } else {
                     if (tabSymb[idIdent].categorie == CONSTANTE) {
                         UtilLex.messErr("Modification d'une constante.");
                     } else {
                         typeIdent = tabSymb[idIdent].type;
                         if (typeIdent == ENT) {
                             po.produire(LIRENT);
                         } else if (typeIdent == BOOL) {
                             po.produire(LIREBOOL);
                         }
                         if (bc != 1 && tabSymb[idIdent].categorie == VARLOCALE) {
                             po.produire(AFFECTERL);
                             po.produire(tabSymb[idIdent].info);
                             po.produire(0);
                         } else if (bc != 1 && tabSymb[idIdent].categorie == PARAMMOD) {
                             po.produire(AFFECTERL);
                             po.produire(tabSymb[idIdent].info);
                             po.produire(1);
                         } else if (tabSymb[idIdent].categorie == VARGLOBALE) {
                             po.produire(AFFECTERG);
                             po.produire(tabSymb[idIdent].info);
                         } else {
                             UtilLex.messErr("Catégorie incompatible avec la lecture");
                         }
                     }
                 }
                 break;
 
             case 29:
                 // 'ecrire', écriture à l'écran
                 if (tCour == ENT) {
                     po.produire(ECRENT);
                 } else if (tCour == BOOL) {
                     po.produire(ECRBOOL);
                 }
                 break;
 
             case 30:
                 // Lecture de 'Alors' et de 'Faire' et d'une condition d'un cond
                 po.produire(BSIFAUX);
                 po.produire(0);
 
                 pileRep.empiler(po.getIpo());
                 break;
 
             case 31:
                 // Sinon
                 po.produire(BINCOND);
                 po.produire(0);
 
                 po.modifier(pileRep.depiler(), po.getIpo() + 1);
                 pileRep.empiler(po.getIpo());
                 break;
 
             case 32:
                 // Fin Si
                 po.modifier(pileRep.depiler(), po.getIpo() + 1);
                 break;
 
             case 33:
                 // Lecture du 'ttq'
                 pileRep.empiler(po.getIpo() + 1);
                 break;
 
             //PtGen avant 'faire' est identique au case 30
 
             case 34:
                 // Lecture du 'fait'
                 po.produire(BINCOND);
                 po.modifier(pileRep.depiler(), po.getIpo() + 2);
                 po.produire(pileRep.depiler());
                 break;
 
             case 35:
                 // Lecture cond
                 pileRep.empiler(0);
                 break;
 
             case 36:
                 // Fin de la condition du cond
                 po.produire(BSIFAUX);
                 po.produire(0);
                 pileRep.empiler(po.getIpo());
                 break;
 
             case 37:
                 // Virgule entre 2 éléments de cond
                 po.modifier(pileRep.depiler(), po.getIpo() + 3);
                 po.produire(BINCOND);
                 po.produire(pileRep.depiler());
                 pileRep.empiler(po.getIpo());
                 break;
 
             case 38:
                 // Lecture de aut
                 po.produire(BINCOND);
                 po.produire(0);
                 po.modifier(pileRep.depiler(), po.getIpo() + 1);
                 pileRep.empiler(po.getIpo());
                 break;
 
             case 39:
                // Lecture FinCond
                po.modifier(pileRep.depiler(), po.getIpo() + 1);
                int aModifier = pileRep.depiler();
                while(aModifier != 0) {
                    int tmp = po.getElt(aModifier);
                    po.modifier(aModifier, po.getIpo()+1);
                    aModifier = tmp;
                }
                break;
 
             case 40:
                 // Déclaration d'une procédure (polyP34, point d'observation 1)
                 po.produire(BINCOND);
                 po.produire(0);
 
                 pileRep.empiler(po.getIpo());
                 break;
 
             case 41:
                 // Lecture d'un ident de proc (polyP34, point d'observation 2)
                 if (presentIdent(bc) == 0) {
                     nbVarL = 0;
 
                     ident = UtilLex.numIdCourant;
 
                     placeIdent(ident, PROC, NEUTRE, po.getIpo()+1);
                     placeIdent(-1, PRIVEE, NEUTRE, 0);
 
                     //changement de contexte (polyP35, point d'observation 3)
                     bc = it + 1;
 
                     nbParamProc = 0;
                 } else {
                     UtilLex.messErr("Une procédure porte déjà ce nom.");
                 }
                 break;
 
             case 42:
                 // Déclaration d'un paramètre fixe (polyP35, point d'observation 4)
                 if (presentIdent(bc) != 0) {
                     UtilLex.messErr("Erreur deux paramètres portent le même nom.");
                 } else {
                     ident = UtilLex.numIdCourant;
                     if (tCour == ENT) {
                         placeIdent(ident, PARAMFIXE, ENT, nbParamProc);
                     } else {
                         placeIdent(ident, PARAMFIXE, BOOL, nbParamProc);
                     }
 
                     nbParamProc += 1;
                 }
                 break;
 
             case 43:
                 // Déclaration d'un paramètre modifiable (polyP35, point d'observation 4)
                 if (presentIdent(bc) != 0) {
                     UtilLex.messErr("Erreur deux variables portent le même nom.");
                 } else {
                     ident = UtilLex.numIdCourant;
 
                     if (tCour == ENT) {
                         placeIdent(ident, PARAMMOD, ENT, nbParamProc);
                     } else {
                         placeIdent(ident, PARAMMOD, BOOL, nbParamProc);
                     }
 
                     nbParamProc += 1;
                 }
                 break;
 
             case 44:
                 // Fin de la déclaration des paramètres d'une proc (polyP35, point d'observation 4)
                 tabSymb[bc - 1].info = nbParamProc;
                 infoVarL = nbParamProc + 2;
 
                 // déclarations des varlocales et const en case 8
                 break;
 
             case 45:
                 // Fin de la déclaration d'une procédure (polyP35, point d'observation 5)
 
                 while (it > bc && (tabSymb[it].categorie == VARLOCALE || tabSymb[it].categorie == CONSTANTE)) {
                     it--;
                 }
                 int tmp = it;
                 while (tabSymb[tmp].categorie == PARAMMOD || tabSymb[tmp].categorie == PARAMFIXE) {
                     tabSymb[tmp].code = -1;
                     tmp--;
                 }
                 po.produire(RETOUR);
                 po.produire(nbParamProc);
 
                 nbParamProc = 0;
                 bc = 1;
 
                 break;
             
             case 46:
                 // Fin de déclaration de procédure (polyP35, point d'observation 6)
                 po.modifier(pileRep.depiler(), po.getIpo() + 1);
                 break;
            
             case 47 :
                 // Contrôle de type sur le passage en paramètre fixe
                if (tabSymb[procActive+nbParamAppel+2].type != tCour) {
                    UtilLex.messErr("Paramètre de mauvais type");
                } else {
                    nbParamAppel++;
                }
                break;
              
             case 48:
                // Transmission en paramètre mod
                idIdent = presentIdent(1);
                if (idIdent == 0) {
                    UtilLex.messErr(UtilLex.chaineIdent(UtilLex.numIdCourant) + " n'est pas dans la table des symboles ");
                } else if (tabSymb[procActive+nbParamAppel+2].type != tabSymb[idIdent].type) {
                    UtilLex.messErr("Paramètre de mauvais type");
                } else {
                    switch (tabSymb[idIdent].categorie) {
                        case VARGLOBALE:
                             po.produire(EMPILERADG);
                             po.produire(tabSymb[idIdent].info);
                             nbParamAppel++;
                             break;
                        case VARLOCALE:
                             po.produire(EMPILERADL);
                             po.produire(tabSymb[idIdent].info);
                             po.produire(0);
                             nbParamAppel++;
                             break;
                        case PARAMMOD:
                             po.produire(EMPILERADL);
                             po.produire(tabSymb[idIdent].info);
                             po.produire(1);
                             nbParamAppel++;
                             break;
                        default:
                             UtilLex.messErr("Catégorie : "+tabSymb[idIdent].categorie+" non compatible lors d'un appel");
                             break;
                    }
                }
                break;
 
             case 49:
                 // Appel
                if (nbParamAppel == tabSymb[procActive + 1].info) { // contrôle du nombre de paramètres
                    po.produire(APPEL);
                    po.produire(tabSymb[procActive].info); // adresse de début de proc
                    po.produire(tabSymb[procActive + 1].info); // nombre de paramètres
                } else {
                    UtilLex.messErr("Nombre de paramètres ("+nbParamAppel+") dans l'appel ne correspond pas au nombre dans la déclration de la procédure.");
                }
                break;
 
              //TODO 
             case 254:
                 // Lecture du 'Fin'
                 po.produire(ARRET);
                 break;
 
             case 255:
                 afftabSymb();  // affichage de la table des symboles en fin de compilation
                 po.constObj(); // production du fichier contenant le code MAPILE
                 po.constGen(); // production du fichier contenant les actions MAPILE
                 break;
 
 
             default:
                 System.out.println("Point de generation non prevu dans votre liste");
                 break;
 
         }
     }
 }
 
 
 
 