# OpResearchProjetDiPlacido

Auteur: Bence Di Placido

Petit projet Java/Gradle pour manipuler des graphes de flot.

Le programme lit un graphe depuis un fichier texte, calcule soit un flot maximum
simple, soit un flot maximum de coût minimum, puis génère des fichiers DOT/PDF
pour visualiser le graphe avant et après le calcul.

Les fichiers d'entrée et de sortie sont placés à la racine du projet.


## Prerequis

- Java JDK 17 ou plus récent. Le projet a été réalisé avec OpenJDK 17.


## Lancer le projet

Depuis la racine du projet:

```bash
./gradlew run
```

Par défaut, le programme utilise `resources/exemple.txt` et lance Ford-Fulkerson sans
prendre les coûts en compte.

Pour choisir explicitement le fichier, l'algorithme et l'export des étapes résiduelles:

```bash
./gradlew run --args="resources/exemple.txt false false"
./gradlew run --args="resources/exemple.txt true false"
./gradlew run --args="resources/exemple.txt false true"
./gradlew run --args="resources/exemple.txt true true"
```

`false` lance Ford-Fulkerson (Edmonds-Karp).
`true` lance la version avec coûts, basée sur Bellman-Ford dans le graphe
résiduel.
Le troisième paramètre active ou non la génération des PDF des graphes
résiduels à chaque étape dans `resources/steps/<nom_du_fichier>/`.

## Fichiers generes

Après l'exécution, les fichiers de visualisation sont placés ici:

- `resources/initials/graph.pdf`: graphe avant le calcul.
- `resources/results/graph.pdf`: graphe après le calcul.
- `resources/steps/graph/step_X.pdf`: graphes résiduels par étape, si l'option est activée.

## Format du fichier d'entree

La première ligne contient:

```text
nombre_noeuds nombre_arcs source puits
```

Chaque ligne suivante décrit un arc:

```text
source destination capacite cout
```


## Tests

```bash
./gradlew test
```

Les tests vérifient notamment que le flot maximum attendu est trouvé et que
l'algorithme avec coûts détecte un cycle de coût négatif atteignable.
