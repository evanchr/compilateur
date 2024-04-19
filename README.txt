COMPILATEUR V3 - HEGO Valentin, MONTIEGE Hugo, CHARRIER Evan

Elements fonctionnels :
	- Tout jusqu'au procédures

Elements ,on fonctionnels :
	- Compilation séparéé
	- Edition de liens

Problèmes rencontrés :
	- Par manque de temps, nous avons préféré nous concentrer sur le
	  fonctionnement des procédures (qui est complètement fonctionnel selon nous)
	  et avons choisi de ne pas commencer la compilation séparéé et l'édition de liens.


COMPILATEUR V2 

Elements fonctionnels :
	- déclaration des procédures
	- table des symboles

Elements non fonctionnels :
	- vérification des paramètres passés lors de l'appel 
	- certains programmes ne fonctionnent pas

Problèmes rencontrés : 
	- Valentin et Evan ayant été absents pendant plusieurs jours (justifié),
	  nous avons pris du retard dans notre compilateur. Nous avons essayé de rattraper
	  au maximum cette semaine mais cela n'a pas été suffisant. 
	  Nous ne sommes donc pas entièrement prêts pour ce deuxième rendu,
	  mais cela devrait être résolu pour le rendu final.


COMPILATEUR V1 

Compilations couvertes par notre compilateur :
    - déclarations
    - expressions
    - instructions de base
    - structures de contrôle si, ttq, et cond

Eventuelles difficultés rencontrées : 
	- Nous pensions que lors de l'affectation d'une variable, 
	  il fallait modifier la table des symboles en changeant la valeur de la colonne 'info'
	  par la valeur affectée à la variable (un peu comme pour les constantes). 
	  Cela nous a bloqué un certain temps alors que notre code était correct 
	  mais nous ne comprenions pas pourquoi il ne marchait pas.
	
	- Au début nous empilions les valeurs lors des déclarations de constantes, 
	  ce qui générait du code MAPILE avant même de commencer le coeur du programme.

Possibilités d'améliorations : 
	- Lors de la lecture d'une valeur, nous avons du code qui se répète. 
	  Pour éviter cela il aurait été judicieux d'ajouter un point de génération
	  après la lecture d'une valeur peu importe qu'il s'agisse d'un entier 
	  positif/négatif ou d'un booléen afin d'empiler cette valeur 
	  (en plus du code qui change tCour et vCour).
	  Nous ne l'avons pas fait car cela reste fonctionnel et nous ne voulions pas 
	  désorganiser tout notre code.