package org.alg;

import org.builder.GraphBuilder;
import org.junit.jupiter.api.Test;
import org.model.EndNode;
import org.model.Graph;
import org.model.MiddleNode;
import org.model.StartNode;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MinCostMaxFlowTest {

    @Test
    void computesMinCostMaxFlow() throws Exception {
        Graph graph = new GraphBuilder().build(Path.of("src/main/resources/graph.txt"));

        Graph solution = new MinCostMaxFlow().computeMaxFlow(graph);

        assertEquals(15, solution.getMaxFlow());
        assertEquals(149, solution.getCost());
    }

    @Test
    void rejectsReachableNegativeCostCycle() {
        Graph graph = graphWithNegativeCostCycle();

        assertThrows(IllegalArgumentException.class, () -> new MinCostMaxFlow().computeMaxFlow(graph));
    }

    private Graph graphWithNegativeCostCycle() {
        GraphBuilder builder = new GraphBuilder();
        StartNode s = builder.createStartNode(0, "s");
        MiddleNode a = builder.createMiddleNode(1, "a");
        MiddleNode b = builder.createMiddleNode(2, "b");
        EndNode t = builder.createEndNode(3, "t");

        builder.connect(s, a, 1, 0);
        builder.connect(a, b, 1, -2);
        builder.connect(b, a, 1, -1);
        builder.connect(b, t, 1, 0);

        return new Graph(s, t, List.of(a, b));
    }
}
