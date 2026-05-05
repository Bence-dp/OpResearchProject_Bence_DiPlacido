package org.builder;

import org.model.Arc;
import org.model.EndNode;
import org.model.Graph;
import org.model.MiddleNode;
import org.model.Node;
import org.model.StartNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResidualGraphBuilder {

    public Graph buildFrom(Graph graph) {
        Map<Node, Node> originalToResidual = new HashMap<>();

        // On garde une copie des noeuds pour ne pas mélanger les capacités résiduelles
        // avec les capacités du graphe initial.
        StartNode residualStart = new StartNode();
        copyNodeData(graph.getStartNode(), residualStart);
        originalToResidual.put(graph.getStartNode(), residualStart);

        EndNode residualEnd = new EndNode();
        copyNodeData(graph.getEndNode(), residualEnd);
        originalToResidual.put(graph.getEndNode(), residualEnd);

        List<Node> residualMiddleNodes = new ArrayList<>();
        for (Node originalNode : graph.getNodeList()) {
            Node residualNode = new MiddleNode();
            copyNodeData(originalNode, residualNode);
            residualMiddleNodes.add(residualNode);
            originalToResidual.put(originalNode, residualNode);
        }

        Graph residualGraph = new Graph(residualStart, residualEnd, residualMiddleNodes);
        Map<Arc, Arc> residualToOriginal = new HashMap<>();
        Map<Arc, Boolean> residualIsReverse = new HashMap<>();
        Map<Arc, Arc> residualReverseArc = new HashMap<>();

        for (Node originalNode : allNodes(graph)) {
            if (originalNode.getArcsSortant() == null) {
                continue;
            }
            for (Arc originalArc : originalNode.getArcsSortant()) {
                addResidualPair(originalArc, originalToResidual, residualToOriginal, residualIsReverse, residualReverseArc);
            }
        }

        graph.setResidualGraph(residualGraph);
        graph.setResidualToOriginal(residualToOriginal);
        graph.setResidualIsReverse(residualIsReverse);
        graph.setResidualReverseArc(residualReverseArc);

        return residualGraph;
    }

    private void copyNodeData(Node source, Node target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setArcsSortant(new ArrayList<>());
    }

    private List<Node> allNodes(Graph graph) {
        List<Node> nodes = new ArrayList<>();
        nodes.add(graph.getStartNode());
        nodes.addAll(graph.getNodeList());
        nodes.add(graph.getEndNode());
        return nodes;
    }

    private void addResidualPair(
            Arc originalArc,
            Map<Node, Node> originalToResidual,
            Map<Arc, Arc> residualToOriginal,
            Map<Arc, Boolean> residualIsReverse,
            Map<Arc, Arc> residualReverseArc
    ) {
        Node residualSource = originalToResidual.get(originalArc.getSource());
        Node residualDestination = originalToResidual.get(originalArc.getDestination());
        if (residualSource == null || residualDestination == null) {
            throw new IllegalArgumentException("Arc lié à un noeud absent du graphe.");
        }

        // Un arc direct porte la capacité restante, l'arc inverse permet d'annuler du flot.
        Arc forward = new Arc();
        forward.setSource(residualSource);
        forward.setDestination(residualDestination);
        forward.setCapacity(originalArc.getInitialCapacity());
        forward.setInitialCapacity(originalArc.getInitialCapacity());
        forward.setCost(originalArc.getCost());
        forward.setFlow(0);

        Arc reverse = new Arc();
        reverse.setSource(residualDestination);
        reverse.setDestination(residualSource);
        reverse.setCapacity(0);
        reverse.setInitialCapacity(0);
        reverse.setCost(-originalArc.getCost());
        reverse.setFlow(0);

        residualSource.getArcsSortant().add(forward);
        residualDestination.getArcsSortant().add(reverse);

        residualToOriginal.put(forward, originalArc);
        residualToOriginal.put(reverse, originalArc);
        residualIsReverse.put(forward, Boolean.FALSE);
        residualIsReverse.put(reverse, Boolean.TRUE);
        residualReverseArc.put(forward, reverse);
        residualReverseArc.put(reverse, forward);
    }
}
