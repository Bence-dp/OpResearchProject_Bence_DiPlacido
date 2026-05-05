package org.main;

import org.alg.MinCostMaxFlow;
import org.alg.FordFulkerson;
import org.alg.MaxFlowAlgorithm;
import org.builder.GraphBuilder;
import org.model.Arc;
import org.model.Graph;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    private static final String DEFAULT_FILE_PATH = "src/main/resources/graph.txt";

    public static void main(String[] args) {
        if (args.length > 2) {
            printUsage();
            return;
        }

        String filePath = args.length > 0 ? args[0] : DEFAULT_FILE_PATH;
        boolean useCosts = args.length > 1 && parseUseCosts(args[1]);

        GraphBuilder graphBuilder = new GraphBuilder();
        DotPdfExporter dotPdfExporter = new DotPdfExporter();
        Path inputFilePath = Path.of(filePath);

        try {
            Graph graph = graphBuilder.build(inputFilePath);
            dotPdfExporter.export(graph, inputFilePath, "initials", graph.getCost(), graph.getMaxFlow());
            System.out.println(graph);

            MaxFlowAlgorithm maxFlowAlgorithm = selectAlgorithm(useCosts);
            Graph solution = maxFlowAlgorithm.computeMaxFlow(graph);
            dotPdfExporter.export(solution, inputFilePath, "results", solution.getCost(), solution.getMaxFlow());

            System.out.println("Max flow: " + solution.getMaxFlow());
            System.out.println("Cost: " + solution.getCost());

            System.out.println(solution);
            if (solution.getMinCutEdges() != null) {
                System.out.println("Arcs de la coupe minimale:");
                for (Arc arc : solution.getMinCutEdges()) {
                    System.out.println("  " + arc);
                }
            }

        } catch (IOException | RuntimeException exception) {
            System.err.println("Impossible d'exécuter le programme: " + exception.getMessage());
        }
    }

    private static MaxFlowAlgorithm selectAlgorithm(boolean useCosts) {
        if (useCosts) {
            return new MinCostMaxFlow();
        }
        return new FordFulkerson();
    }

    private static boolean parseUseCosts(String value) {
        String normalized = value.trim().toLowerCase();
        switch (normalized) {
            case "true":
                return true;
            case "false":
                return false;
            default:
                throw new IllegalArgumentException(
                        "Option coûts invalide: " + value + ". Utilise true/false"
                );
        }
    }

    private static void printUsage() {
        System.err.println("Usage: ./gradlew run --args=\"<fichier.txt> <avecCouts>\"");
        System.err.println("Exemple sans coûts: ./gradlew run --args=\"src/main/resources/graph.txt false\"");
        System.err.println("Exemple avec coûts: ./gradlew run --args=\"src/main/resources/graph.txt true\"");
    }
}
