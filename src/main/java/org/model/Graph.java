package org.model;

import java.util.List;

public class Graph {
    private StartNode startNode;
    private EndNode endNode;
    private List<Node> nodeList;
    private Graph residualGraph;
    private int maxFlow;
    private List<Arc> minCutEdges;

    /**
     * @param startNode
     * @param endNode
     * @param nodeList
     */
    public Graph(StartNode startNode, EndNode endNode, List<Node> nodeList) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.nodeList = nodeList;
    }

    public Graph(StartNode startNode, EndNode endNode, List<Node> nodeList, Graph residualGraph) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.nodeList = nodeList;
        this.residualGraph = residualGraph;
    }

    public int getMaxFlow() {
        return maxFlow;
    }

    public void setMaxFlow(int maxFlow) {
        this.maxFlow = maxFlow;
    }

    public java.util.List<Arc> getMinCutEdges() {
        return minCutEdges;
    }

    public void setMinCutEdges(java.util.List<Arc> minCutEdges) {
        this.minCutEdges = minCutEdges;
    }

    public StartNode getStartNode() {
        return startNode;
    }

    public void setStartNode(StartNode startNode) {
        this.startNode = startNode;
    }

    public EndNode getEndNode() {
        return endNode;
    }

    public void setEndNode(EndNode endNode) {
        this.endNode = endNode;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public Graph getResidualGraph() {
        return residualGraph;
    }

    public void setResidualGraph(Graph residualGraph) {
        this.residualGraph = residualGraph;
    }

    public String graphToString(){
        StringBuilder builder = new StringBuilder();

        if (startNode != null && startNode.getArcsSortant() != null) {
            for (Arc arc : startNode.getArcsSortant()) {
                builder.append("              ")
                        .append(arc)
                        .append("\n");
            }
        }

        if (nodeList != null) {
            for (Node node : nodeList) {
                if (node == null || node.getArcsSortant() == null) {
                    continue;
                }
                for (Arc arc : node.getArcsSortant()) {
                    builder.append("              ")
                            .append(arc)
                            .append("\n");
                }
            }
        }

        if (endNode != null && startNode != null) {
            builder.append("              ")
                    .append(endNode.getName())
                    .append(" -> ")
                    .append(startNode.getName())
                    .append(" [color=red]\n");
        }

        return builder.toString().trim();
    }
    public String nodeListToString(){
        StringBuilder builder = new StringBuilder();

        if (startNode != null) {
            builder.append("              ")
                    .append(startNode.getName())
                    .append(" [label=\"")
                    .append(startNode.getName())
                    .append("\",color=green]\n");
        }

        if (nodeList != null) {
            for (Node node : nodeList) {
                if (node == null) {
                    continue;
                }
                builder.append("              ")
                        .append(node.getName())
                        .append(" [label=\"")
                        .append(node.getName())
                        .append("\"]\n");
            }
        }

        if (endNode != null) {
            builder.append("              ")
                    .append(endNode.getName())
                    .append(" [label=\"")
                    .append(endNode.getName())
                    .append("\",color=blue]\n");
        }

        return builder.toString().trim();
    }
    @Override
    public String toString() {
        return "digraph Gv2{     \n" +
                "graph [nodesep=\"0.3\", ranksep=\"0.3\",fontsize=12]\n" +
                "node [shape=circle,fixedsize=true,width=.3,height=.3,fontsize=12]\n" +
                "edge [arrowsize=0.6]\n\n" +
                graphToString() +
                "\n\n" +
                nodeListToString() +
                "\n}";
    }
}
