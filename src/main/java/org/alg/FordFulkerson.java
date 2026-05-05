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
            throw new IllegalArgumentException("Graph, start or end node is null");
        }

        resetResult(graph);
        Graph residual = residualGraphBuilder.buildFrom(graph);

        Node s = residual.getStartNode();
        Node t = residual.getEndNode();
        Map<Arc, Arc> reverseArcs = graph.getResidualReverseArc();
        Map<Arc, Arc> residualToOriginal = graph.getResidualToOriginal();
        Map<Arc, Boolean> residualIsReverse = graph.getResidualIsReverse();

        int maxFlow = 0;

        while (true) {
            Map<Node, Arc> parentArc = new HashMap<>();

            boolean found = bfsFindPath(s, t, parentArc);
            if (!found) break;

            int bottleneck = Integer.MAX_VALUE;
            Node cur = t;
            while (cur != s) {
                Arc resArc = parentArc.get(cur);
                if (resArc == null) {
                    throw new IllegalStateException("Chemin augmentant incomplet dans le graphe résiduel.");
                }
                bottleneck = Math.min(bottleneck, resArc.getCapacity());
                cur = resArc.getSource();
            }
            if (bottleneck == 0 || bottleneck == Integer.MAX_VALUE) {
                break;
            }

            cur = t;
            while (cur != s) {
                Arc resArc = parentArc.get(cur);
                Arc reverseArc = reverseArcs.get(resArc);
                if (reverseArc == null) {
                    throw new IllegalStateException("Arc inverse manquant dans le graphe résiduel.");
                }

                resArc.setCapacity(resArc.getCapacity() - bottleneck);
                reverseArc.setCapacity(reverseArc.getCapacity() + bottleneck);

                Arc originalArc = residualToOriginal.get(resArc);
                Boolean reverse = residualIsReverse.get(resArc);
                if (originalArc == null || reverse == null) {
                    throw new IllegalStateException("Correspondance vers l'arc original manquante.");
                }
                if (reverse) {
                    originalArc.setFlow(originalArc.getFlow() - bottleneck);
                } else {
                    originalArc.setFlow(originalArc.getFlow() + bottleneck);
                }

                cur = resArc.getSource();
            }

            maxFlow += bottleneck;
        }

        Set<Node> reachable = collectReachable(s);
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

    private boolean bfsFindPath(Node s, Node t, Map<Node, Arc> parentArc) {
        Queue<Node> queue = new ArrayDeque<>();
        Set<Node> visited = new HashSet<>();
        queue.add(s);
        visited.add(s);

        while (!queue.isEmpty()) {
            Node u = queue.poll();
            List<Arc> arcs = u.getArcsSortant();
            if (arcs == null) continue;
            for (Arc arc : arcs) {
                if (arc.getCapacity() <= 0) continue;
                Node v = arc.getDestination();
                if (visited.contains(v)) continue;
                parentArc.put(v, arc);
                visited.add(v);
                if (v == t) return true;
                queue.add(v);
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
