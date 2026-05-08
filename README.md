# OpResearchProjetDiPlacido

Auteur: Bence Di Placido

Petit projet Java/Gradle pour manipuler des graphes de flot.

Le programme lit un graphe depuis un fichier texte, calcule soit un flot maximum
simple, soit un flot maximum de coût minimum, puis génère des fichiers DOT/PDF
pour visualiser le graphe avant et après le calcul.

Les résultats des flot et min cost demandé en cours sont présents dans `src/main/resources/`


## Prerequis

- Java JDK 17 ou plus récent. Le projet a été réalisé avec OpenJDK 17.


## Lancer le projet

Depuis la racine du projet:

```bash
./gradlew run
```

Par défaut, le programme utilise `src/main/resources/graph.txt` et lance
Ford-Fulkerson sans prendre les coûts en compte.

Pour choisir explicitement le fichier et l'algorithme:

```bash
./gradlew run --args="src/main/resources/graph.txt false"
./gradlew run --args="src/main/resources/graph.txt true"
```

`false` lance Ford-Fulkerson (Edmonds-Karp).
`true` lance la version avec coûts, basée sur Bellman-Ford dans le graphe
résiduel.

## Fichiers generes

Après l'exécution, les fichiers de visualisation sont placés ici:

- `src/main/resources/initials/graph.pdf`: graphe avant le calcul.
- `src/main/resources/initials/graph.dot`: fichier DOT correspondant.
- `src/main/resources/results/graph.pdf`: graphe après le calcul.
- `src/main/resources/results/graph.dot`: fichier DOT correspondant.

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
