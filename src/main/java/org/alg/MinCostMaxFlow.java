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

public class MinCostMaxFlow implements MaxFlowAlgorithm {
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

        // On augmente le flot tant qu'il existe un chemin de coût minimal dans le graphe résiduel.
        while (true) {
            Map<Node, Arc> parentArc = new HashMap<>();
            boolean pathFound = bellmanFordFindShortestPath(residual, source, sink, parentArc);
            if (!pathFound) {
                break;
            }

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

            int pathCost = 0;
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

                pathCost += residualArc.getCost();
                currentNode = residualArc.getSource();
            }

            maxFlow += bottleneck;
            graph.addCost(pathCost * bottleneck);
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

    private boolean bellmanFordFindShortestPath(Graph residual, Node source, Node sink, Map<Node, Arc> parentArc) {
        List<Node> nodes = allNodes(residual);
        Map<Node, Integer> distances = new HashMap<>();
        for (Node node : nodes) {
            distances.put(node, Integer.MAX_VALUE);
        }
        distances.put(source, 0);

        // Bellman-Ford accepte les coûts négatifs, ce qui est utile pour les arcs inverses.
        for (int i = 0; i < nodes.size() - 1; i++) {
            boolean changed = false;
            for (Node node : nodes) {
                Integer sourceDistance = distances.get(node);
                if (sourceDistance == null || sourceDistance == Integer.MAX_VALUE || node.getArcsSortant() == null) {
                    continue;
                }
                for (Arc arc : node.getArcsSortant()) {
                    if (arc.getCapacity() <= 0) {
                        continue;
                    }
                    Node destination = arc.getDestination();
                    int newDistance = sourceDistance + arc.getCost();
                    if (newDistance < distances.get(destination)) {
                        distances.put(destination, newDistance);
                        parentArc.put(destination, arc);
                        changed = true;
                    }
                }
            }
            if (!changed) {
                break;
            }
        }

        for (Node node : nodes) {
            Integer sourceDistance = distances.get(node);
            if (sourceDistance == null || sourceDistance == Integer.MAX_VALUE || node.getArcsSortant() == null) {
                continue;
            }
            for (Arc arc : node.getArcsSortant()) {
                if (arc.getCapacity() <= 0) {
                    continue;
                }
                if (sourceDistance + arc.getCost() < distances.get(arc.getDestination())) {
                    throw new IllegalArgumentException("Cycle de coût négatif détecté dans le graphe résiduel.");
                }
            }
        }

        return distances.get(sink) != Integer.MAX_VALUE;
    }

    private Set<Node> collectReachable(Node source) {
        Set<Node> reachable = new HashSet<>();
        Queue<Node> queue = new ArrayDeque<>();
        queue.add(source);
        reachable.add(source);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            if (u.getArcsSortant() == null) {
                continue;
            }
            for (Arc arc : u.getArcsSortant()) {
                Node v = arc.getDestination();
                if (arc.getCapacity() <= 0 || reachable.contains(v)) {
                    continue;
                }
                reachable.add(v);
                queue.add(v);
            }
        }
        return reachable;
    }

    private void resetResult(Graph graph) {
        graph.setMaxFlow(0);
        graph.setCost(0);
        graph.setMinCutEdges(new ArrayList<>());
        for (Node node : allNodes(graph)) {
            if (node.getArcsSortant() == null) {
                continue;
            }
            for (Arc arc : node.getArcsSortant()) {
                arc.setFlow(0);
            }
        }
    }

    private List<Node> allNodes(Graph graph) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(graph.getStartNode());
        nodes.addAll(graph.getNodeList());
        nodes.add(graph.getEndNode());
        return nodes;
    }
}
