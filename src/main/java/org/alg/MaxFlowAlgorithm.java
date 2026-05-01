package org.alg;

import org.model.Graph;

public interface MaxFlowAlgorithm {
    /**
     * Compute max flow on the provided graph. Implementation may mutate the graph (residual capacities).
     * The returned Graph should contain solution information (maxFlow and minCutEdges).
     * @param graph the input graph (start/end and node list must be set)
     * @return the graph containing the solution (may be the same instance)
     */
    Graph computeMaxFlow(Graph graph);
}
