package org.alg;

import org.model.Arc;
import org.model.Graph;
import org.model.Node;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class FordFulkerson implements MaxFlowAlgorithm {

    @Override
    public Graph computeMaxFlow(Graph graph) {
        if (graph == null || graph.getStartNode() == null || graph.getEndNode() == null) {
            throw new IllegalArgumentException("Graph, start or end node is null");
        }

        Graph residual = graph.getResidualGraph();
        if (residual == null) {
            throw new IllegalArgumentException("Residual graph missing on Graph");
        }

        Node s = residual.getStartNode();
        Node t = residual.getEndNode();

        int maxFlow = 0;

        while (true) {
            Map<Node, Arc> parentArc = new HashMap<>();

            boolean found = bfsFindPath(s, t, parentArc);
            if (!found) break;

            int bottleneck = Integer.MAX_VALUE;
            Node cur = t;
            while (cur != s) {
                Arc resArc = parentArc.get(cur);
                if (resArc == null) break;
                bottleneck = Math.min(bottleneck, resArc.getCapacity());
                cur = resArc.getSource();
            }
            if (bottleneck == 0 || bottleneck == Integer.MAX_VALUE) break;

            cur = t;
            while (cur != s) {
                Arc resArc = parentArc.get(cur);
                Node u = resArc.getSource();
                Node v = resArc.getDestination();

                // decrease forward residual capacity
                resArc.setCapacity(resArc.getCapacity() - bottleneck);

                // increase reverse residual capacity
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

                // update original arc's flow a
                Map<Arc, Arc> resToOrig = graph.getResidualToOriginal();
                Map<Arc, Boolean> resIsRev = graph.getResidualIsReverse();
                if (resToOrig != null && resIsRev != null) {
                    Arc orig = resToOrig.get(resArc);
                    Boolean isRev = resIsRev.get(resArc);
                    if (orig != null && isRev != null) {
                        if (isRev) {
                            orig.setFlow(orig.getFlow() - bottleneck);
                        } else {
                            orig.setFlow(orig.getFlow() + bottleneck);
                        }
                    }
                }

                cur = u;
            }

            maxFlow += bottleneck;
        }

        // compute reachable set on residual graph
        Set<Node> reachable = new HashSet<>();
        collectReachable(s, reachable);

        List<Arc> minCut = new java.util.ArrayList<>();
        Map<Arc, Arc> resToOrig = graph.getResidualToOriginal();
        Map<Arc, Boolean> resIsRev = graph.getResidualIsReverse();
        if (resToOrig != null && resIsRev != null) {
            for (Map.Entry<Arc, Arc> e : resToOrig.entrySet()) {
                Arc resArc = e.getKey();
                Arc origArc = e.getValue();
                Boolean isReverse = resIsRev.get(resArc);
                if (isReverse != null && !isReverse) {
                    Node uRes = resArc.getSource();
                    Node vRes = resArc.getDestination();
                    if (reachable.contains(uRes) && !reachable.contains(vRes) && origArc.getInitialCapacity() > 0) {
                        if (!minCut.contains(origArc)) minCut.add(origArc);
                    }
                }
            }
        }

        graph.setMaxFlow(maxFlow);
        graph.setMinCutEdges(minCut);

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
                if (v == t) return true;
                visited.add(v);
                queue.add(v);
            }
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
