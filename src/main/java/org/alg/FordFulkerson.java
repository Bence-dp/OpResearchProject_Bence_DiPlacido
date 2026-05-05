package org.alg;

import org.builder.ResidualGraphBuilder;
import org.model.Arc;
import org.model.Graph;
import org.model.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class FordFulkerson implements MaxFlowAlgorithm {
    private final ResidualGraphBuilder residualGraphBuilder = new ResidualGraphBuilder();

    @Override
    public Graph computeMaxFlow(Graph graph) {
        if (graph == null || graph.getStartNode() == null || graph.getEndNode() == null) {
            throw new IllegalArgumentException("Le graphe, la source ou le puits est nul.");
        }

        resetResult(graph);
        Graph residual = residualGraphBuilder.buildFrom(graph);

        Node source = residual.getStartNode();
        Node sink = residual.getEndNode();
        Map<Arc, Arc> reverseArcs = graph.getResidualReverseArc();
        Map<Arc, Arc> residualToOriginal = graph.getResidualToOriginal();
        Map<Arc, Boolean> residualIsReverse = graph.getResidualIsReverse();

        int maxFlow = 0;

        // Edmonds-Karp: on cherche un chemin augmentant en largeur dans le graphe résiduel.
        while (true) {
            Map<Node, Arc> parentArc = new HashMap<>();

            boolean pathFound = bfsFindPath(source, sink, parentArc);
            if (!pathFound) break;

            int bottleneck = Integer.MAX_VALUE;
            Node currentNode = sink;
            while (currentNode != source) {
                Arc residualArc = parentArc.get(currentNode);
                if (residualArc == null) {
                    throw new IllegalStateException("Chemin augmentant incomplet dans le graphe résiduel.");
                }
                bottleneck = Math.min(bottleneck, residualArc.getCapacity());
                currentNode = residualArc.getSource();
            }
            if (bottleneck == 0 || bottleneck == Integer.MAX_VALUE) {
                break;
            }

            currentNode = sink;
            while (currentNode != source) {
                Arc residualArc = parentArc.get(currentNode);
                Arc reverseArc = reverseArcs.get(residualArc);
                if (reverseArc == null) {
                    throw new IllegalStateException("Arc inverse manquant dans le graphe résiduel.");
                }

                residualArc.setCapacity(residualArc.getCapacity() - bottleneck);
                reverseArc.setCapacity(reverseArc.getCapacity() + bottleneck);

                Arc originalArc = residualToOriginal.get(residualArc);
                Boolean reverse = residualIsReverse.get(residualArc);
                if (originalArc == null || reverse == null) {
                    throw new IllegalStateException("Correspondance vers l'arc original manquante.");
                }
                if (reverse) {
                    originalArc.setFlow(originalArc.getFlow() - bottleneck);
                } else {
                    originalArc.setFlow(originalArc.getFlow() + bottleneck);
                }

                currentNode = residualArc.getSource();
            }

            maxFlow += bottleneck;
        }

        Set<Node> reachable = collectReachable(source);
        Set<Arc> minCut = new LinkedHashSet<>();
        for (Map.Entry<Arc, Arc> entry : residualToOriginal.entrySet()) {
            Arc residualArc = entry.getKey();
            Arc originalArc = entry.getValue();
            boolean reverse = Boolean.TRUE.equals(residualIsReverse.get(residualArc));
            if (!reverse
                    && reachable.contains(residualArc.getSource())
                    && !reachable.contains(residualArc.getDestination())
                    && originalArc.getInitialCapacity() > 0) {
                minCut.add(originalArc);
            }
        }

        graph.setMaxFlow(maxFlow);
        graph.setMinCutEdges(new ArrayList<>(minCut));

        return graph;
    }

    private boolean bfsFindPath(Node source, Node sink, Map<Node, Arc> parentArc) {
        Queue<Node> queue = new ArrayDeque<>();
        Set<Node> visited = new HashSet<>();
        queue.add(source);
        visited.add(source);

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            List<Arc> arcs = currentNode.getArcsSortant();
            if (arcs == null) continue;
            for (Arc arc : arcs) {
                if (arc.getCapacity() <= 0) continue;
                Node nextNode = arc.getDestination();
                if (visited.contains(nextNode)) continue;
                parentArc.put(nextNode, arc);
                visited.add(nextNode);
                if (nextNode == sink) return true;
                queue.add(nextNode);
            }
        }
        return false;
    }

    private Set<Node> collectReachable(Node source) {
        Set<Node> reachable = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(source);
        reachable.add(source);

        while (!queue.isEmpty()) {
            Node currentNode = queue.poll();
            if (currentNode.getArcsSortant() == null) {
                continue;
            }
            for (Arc arc : currentNode.getArcsSortant()) {
                Node nextNode = arc.getDestination();
                if (arc.getCapacity() <= 0 || reachable.contains(nextNode)) {
                    continue;
                }
                reachable.add(nextNode);
                queue.add(nextNode);
            }
        }
        return reachable;
    }

    private void resetResult(Graph graph) {
        graph.setMaxFlow(0);
        graph.setCost(0);
        graph.setMinCutEdges(new ArrayList<>());
        for (Node node : originalNodes(graph)) {
            if (node.getArcsSortant() == null) {
                continue;
            }
            for (Arc arc : node.getArcsSortant()) {
                arc.setFlow(0);
            }
        }
    }

    private List<Node> originalNodes(Graph graph) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(graph.getStartNode());
        nodes.addAll(graph.getNodeList());
        nodes.add(graph.getEndNode());
        return nodes;
    }
}
