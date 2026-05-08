package org.alg;

import org.model.Graph;

public interface MaxFlowAlgorithm {
    /**
     * @param graph 
     * @return graph
     */
    default Graph computeMaxFlow(Graph graph) {
        return computeMaxFlow(graph, null);
    }

    Graph computeMaxFlow(Graph graph, ResidualStepConsumer stepConsumer);
}
