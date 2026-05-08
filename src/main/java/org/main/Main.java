package org.main;

import org.alg.MinCostMaxFlow;
import org.alg.FordFulkerson;
import org.alg.MaxFlowAlgorithm;
import org.alg.ResidualStepConsumer;
import org.builder.GraphBuilder;
import org.model.Arc;
import org.model.Graph;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;

public class Main {
    private static final String DEFAULT_FILE_PATH = "resources/exemple.txt";

    public static void main(String[] args) {
        if (args.length > 3) {
            printUsage();
            return;
        }

        String filePath = args.length > 0 ? args[0] : DEFAULT_FILE_PATH;
        boolean useCosts = args.length > 1 && parseUseCosts(args[1]);
        boolean exportResidualSteps = args.length > 2 && parseUseCosts(args[2]);

        GraphBuilder graphBuilder = new GraphBuilder();
        DotPdfExporter dotPdfExporter = new DotPdfExporter();
        Path inputFilePath = Path.of(filePath);
        String inputBaseName = baseName(inputFilePath);
        Path initialsDirectory = Path.of("resources", "initials");
        Path resultsDirectory = Path.of("resources", "results");
        Path stepsDirectory = Path.of("resources", "steps", inputBaseName);

        try {
            Graph graph = graphBuilder.build(inputFilePath);
            dotPdfExporter.export(graph, initialsDirectory, inputBaseName, graph.getCost(), graph.getMaxFlow());
            //System.out.println(graph);

            MaxFlowAlgorithm maxFlowAlgorithm = selectAlgorithm(useCosts);
            ResidualStepConsumer stepConsumer = exportResidualSteps
                    ? (originalGraph, residualGraph, stepIndex) -> {
                        try {
                            dotPdfExporter.export(
                                    residualGraph,
                                    stepsDirectory,
                                    String.format("step_%03d", stepIndex),
                                    originalGraph.getCost(),
                                    originalGraph.getMaxFlow()
                            );
                        } catch (IOException exception) {
                            throw new UncheckedIOException(exception);
                        }
                    }
                    : null;
            Graph solution = maxFlowAlgorithm.computeMaxFlow(graph, stepConsumer);
            dotPdfExporter.export(solution, resultsDirectory, inputBaseName, solution.getCost(), solution.getMaxFlow());

            System.out.println("Max flow: " + solution.getMaxFlow());
            System.out.println("Cost: " + solution.getCost());

            //System.out.println(solution);
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
        System.err.println("Usage: ./gradlew run --args=\"<fichier.txt> <avecCouts> <exportResiduel>\"");
        System.err.println("Exemple sans coûts: ./gradlew run --args=\"resources/exemple.txt false false\"");
        System.err.println("Exemple avec coûts et étapes: ./gradlew run --args=\"resources/exemple.txt true true\"");
    }

    private static String baseName(Path inputFilePath) {
        String fileName = inputFilePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }
}
