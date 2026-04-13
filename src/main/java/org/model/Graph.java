package org.model;

import java.util.ArrayList;
import java.util.List;

public class Graph {
    private StartNode startNode;
    private EndNode endNode;
    private List<Node> nodeList;

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

    @Override
    public String toString() {
        return "";
    }
}
