package org.alg;

import org.model.Arc;
import org.model.Graph;
import org.model.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FordFulkerson implements MaxFlowAlgorithm {

    @Override
    public Graph computeMaxFlow(Graph graph) {
        if (graph == null || graph.getStartNode() == null || graph.getEndNode() == null) {
            throw new IllegalArgumentException("Graph, start or end node is null");
        }

        Node s = graph.getStartNode();
        Node t = graph.getEndNode();

        int maxFlow = 0;

        while (true) {
            Map<Node, Arc> parentArc = new HashMap<>();
            Set<Node> visited = new HashSet<>();

            boolean found = dfsFindPath(s, t, visited, parentArc);
            if (!found) break;

            int bottleneck = Integer.MAX_VALUE;
            Node cur = t;
            while (cur != s) {
                Arc arc = parentArc.get(cur);
                if (arc == null) break; // defensive
                bottleneck = Math.min(bottleneck, arc.getCapacity());
                cur = arc.getSource();
            }
            if (bottleneck == 0 || bottleneck == Integer.MAX_VALUE) break;

            cur = t;
            while (cur != s) {
                Arc arc = parentArc.get(cur);
                Node u = arc.getSource();
                Node v = arc.getDestination();

                arc.setCapacity(arc.getCapacity() - bottleneck);

                Arc rev = findArc(v, u);
                if (rev == null) {
                    rev = new Arc();
                    rev.setSource(v);
                    rev.setDestination(u);
                    rev.setCapacity(0);
                    rev.setCost(0);
                    List<Arc> out = v.getArcsSortant();
                    if (out != null) out.add(rev);
                }
                rev.setCapacity(rev.getCapacity() + bottleneck);

                cur = u;
            }

            maxFlow += bottleneck;
        }

        Set<Node> reachable = new HashSet<>();
        collectReachable(s, reachable);

        List<Arc> minCut = new java.util.ArrayList<>();
        if (s.getArcsSortant() != null) {
            for (Arc a : s.getArcsSortant()) {
                Node v = a.getDestination();
                if (reachable.contains(s) && !reachable.contains(v) && a.getInitialCapacity() > 0) {
                    minCut.add(a);
                }
            }
        }

        List<Node> nodes = graph.getNodeList();
        if (nodes != null) {
            for (Node n : nodes) {
                if (n.getArcsSortant() == null) continue;
                for (Arc a : n.getArcsSortant()) {
                    Node v = a.getDestination();
                    if (reachable.contains(n) && !reachable.contains(v) && a.getInitialCapacity() > 0) {
                        minCut.add(a);
                    }
                }
            }
        }

        graph.setMaxFlow(maxFlow);
        graph.setMinCutEdges(minCut);

        graph.setMaxFlow(maxFlow);
        graph.setMinCutEdges(minCut);

        return graph;
    }

    private boolean dfsFindPath(Node u, Node sink, Set<Node> visited, Map<Node, Arc> parentArc) {
        if (u == sink) return true;
        visited.add(u);
        List<Arc> arcs = u.getArcsSortant();
        if (arcs == null) return false;
        for (Arc arc : arcs) {
            if (arc.getCapacity() <= 0) continue;
            Node v = arc.getDestination();
            if (visited.contains(v)) continue;
            parentArc.put(v, arc);
            boolean found = dfsFindPath(v, sink, visited, parentArc);
            if (found) return true;
        }
        return false;
    }

    private Arc findArc(Node source, Node dest) {
        if (source == null || source.getArcsSortant() == null) return null;
        for (Arc a : source.getArcsSortant()) {
            if (a.getDestination() == dest) return a;
        }
        return null;
    }

    private void collectReachable(Node u, Set<Node> reachable) {
        if (u == null) return;
        if (reachable.contains(u)) return;
        reachable.add(u);
        if (u.getArcsSortant() == null) return;
        for (Arc a : u.getArcsSortant()) {
            if (a.getCapacity() <= 0) continue;
            collectReachable(a.getDestination(), reachable);
        }
    }
}
