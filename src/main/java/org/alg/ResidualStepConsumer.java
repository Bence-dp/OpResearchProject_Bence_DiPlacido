package org.alg;

import org.model.Graph;

@FunctionalInterface
public interface ResidualStepConsumer {
    void accept(Graph originalGraph, Graph residualGraph, int stepIndex);
}