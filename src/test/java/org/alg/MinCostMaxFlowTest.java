package org.alg;

import org.builder.GraphBuilder;
import org.junit.jupiter.api.Test;
import org.model.Arc;
import org.model.EndNode;
import org.model.Graph;
import org.model.MiddleNode;
import org.model.Node;
import org.model.StartNode;

import java.nio.file.Path;
import java.util.ArrayList;
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
        StartNode s = start(0, "s");
        MiddleNode a = middle(1, "a");
        MiddleNode b = middle(2, "b");
        EndNode t = end(3, "t");

        connect(s, a, 1, 0);
        connect(a, b, 1, -2);
        connect(b, a, 1, -1);
        connect(b, t, 1, 0);

        return new Graph(s, t, List.of(a, b));
    }

    private List<Node> allNodes(Graph graph) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(graph.getStartNode());
        nodes.addAll(graph.getNodeList());
        nodes.add(graph.getEndNode());
        return nodes;
    }

    private StartNode start(int id, String name) {
        StartNode node = new StartNode();
        initialize(node, id, name);
        return node;
    }

    private MiddleNode middle(int id, String name) {
        MiddleNode node = new MiddleNode();
        initialize(node, id, name);
        return node;
    }

    private EndNode end(int id, String name) {
        EndNode node = new EndNode();
        initialize(node, id, name);
        return node;
    }

    private void initialize(Node node, int id, String name) {
        node.setId(id);
        node.setName(name);
        node.setArcsSortant(new ArrayList<>());
    }

    private void connect(Node source, Node destination, int capacity, int cost) {
        Arc arc = new Arc();
        arc.setSource(source);
        arc.setDestination(destination);
        arc.setCapacity(capacity);
        arc.setInitialCapacity(capacity);
        arc.setCost(cost);
        arc.setFlow(0);
        source.getArcsSortant().add(arc);
    }
}
