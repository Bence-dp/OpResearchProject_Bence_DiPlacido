package org.main;

import org.alg.FordFulkerson;
import org.alg.MaxFlowAlgorithm;
import org.model.Arc;
import org.model.Graph;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {

        String filePath = args.length > 0 ? args[0] : "src/main/resources/graph.txt";

        GraphBuilder graphBuilder = new GraphBuilder();
        try {
            Graph graph = graphBuilder.build(Path.of(filePath));
            System.out.println(graph);

                
                MaxFlowAlgorithm ff = new FordFulkerson();
                Graph solution = ff.computeMaxFlow(graph);
                System.out.println("Max flow: " + solution.getMaxFlow());
                System.out.println(graph);
                if (solution.getMinCutEdges() != null) {
                    System.out.println("Min-cut edges:");
                    for (Arc a : solution.getMinCutEdges()) {
                        System.out.println("  " + a.toString());
                    }
                }

        } catch (IOException | RuntimeException exception) {
            System.err.println("Impossible de construire le graphe: " + exception.getMessage());
        }
    }
}