package org.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph {
    private StartNode startNode;
    private EndNode endNode;
    private List<Node> nodeList;
    private Graph residualGraph;
    private boolean residualView;
    private int maxFlow;
    private int cost;
    private List<Arc> minCutEdges;
    private Map<Arc, Arc> residualToOriginal;
    private Map<Arc, Boolean> residualIsReverse;
    private Map<Arc, Arc> residualReverseArc;

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

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void addCost(int cost) {
        this.cost += cost;
    }

    public List<Arc> getMinCutEdges() {
        return minCutEdges;
    }

    public void setMinCutEdges(List<Arc> minCutEdges) {
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

    public boolean isResidualView() {
        return residualView;
    }

    public void setResidualView(boolean residualView) {
        this.residualView = residualView;
    }

    public Map<Arc, Arc> getResidualToOriginal() {
        return residualToOriginal;
    }

    public void setResidualToOriginal(Map<Arc, Arc> residualToOriginal) {
        this.residualToOriginal = residualToOriginal;
    }

    public Map<Arc, Boolean> getResidualIsReverse() {
        return residualIsReverse;
    }

    public void setResidualIsReverse(Map<Arc, Boolean> residualIsReverse) {
        this.residualIsReverse = residualIsReverse;
    }

    public Map<Arc, Arc> getResidualReverseArc() {
        return residualReverseArc;
    }

    public void setResidualReverseArc(Map<Arc, Arc> residualReverseArc) {
        this.residualReverseArc = residualReverseArc;
    }

    public String graphToString(){
        StringBuilder builder = new StringBuilder();
        Set<Arc> minCutSet = new HashSet<>();
        if (this.minCutEdges != null) minCutSet.addAll(this.minCutEdges);

        if (startNode != null && startNode.getArcsSortant() != null) {
            for (Arc arc : startNode.getArcsSortant()) {
                boolean highlight = minCutSet.contains(arc);
                builder.append("              ")
                        .append(arc.getSource().getName())
                        .append(" -> ")
                        .append(arc.getDestination().getName())
                            .append(" [label = <<font color=\"blue\">")
                            .append(displayCapacity(arc))
                            .append("</font>,<font color=\"red\">")
                            .append(arc.getCost())
                            .append("</font>>");
                if (highlight) builder.append(", color=red");
                builder.append("]");
                builder.append("\n");
            }
        }

        if (nodeList != null) {
            for (Node node : nodeList) {
                if (node == null || node.getArcsSortant() == null) {
                    continue;
                }
                for (Arc arc : node.getArcsSortant()) {
                    boolean highlight = minCutSet.contains(arc);
                    builder.append("              ")
                            .append(arc.getSource().getName())
                            .append(" -> ")
                            .append(arc.getDestination().getName())
                            .append(" [label = <<font color=\"blue\">")
                        .append(displayCapacity(arc))
                            .append("</font>,<font color=\"red\">")
                            .append(arc.getCost())
                            .append("</font>>")
                    ;
                    if (highlight) builder.append(", color=red");
                    builder.append("]");
                    builder.append("\n");
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

    private String displayCapacity(Arc arc) {
        if (residualView) {
            return String.valueOf(arc.getCapacity());
        }
        return arc.getFlow() + "/" + arc.getInitialCapacity();
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
