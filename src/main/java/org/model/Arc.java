package org.model;

public class Arc {
    private int cost;
    private int capacity;
    private Node source;
    private Node destination;

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return getSource().getName() + " -> " + getDestination().getName() + "[label = <<font color=\"green\">"+this.getCapacity()+"</font>,<font color=\"red\">"+this.getCost()+"</font>>]";
    }
}
