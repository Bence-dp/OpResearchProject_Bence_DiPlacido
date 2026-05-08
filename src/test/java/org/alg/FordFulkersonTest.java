package org.alg;

import org.junit.jupiter.api.Test;
import org.builder.GraphBuilder;
import org.model.Graph;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FordFulkersonTest {

    @Test
    void computesMaxFlowAndCanRunTwiceOnSameGraph() throws Exception {
        Graph graph = new GraphBuilder().build(Path.of("resources/exemple.txt"));
        FordFulkerson algorithm = new FordFulkerson();

        Graph firstSolution = algorithm.computeMaxFlow(graph);
        assertEquals(15, firstSolution.getMaxFlow());

        Graph secondSolution = algorithm.computeMaxFlow(graph);

        assertEquals(15, secondSolution.getMaxFlow());
        assertEquals(15, secondSolution.getMinCutEdges().stream()
                .mapToInt(arc -> arc.getInitialCapacity())
                .sum());
    }
}
