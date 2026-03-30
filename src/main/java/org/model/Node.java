package org.model;

import java.util.Collection;
import java.util.List;

public abstract class Node {
    private String name;
    private List<Arc> arcs;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Arc> getArcs() {
        return arcs;
    }

    public void setArcs(List<Arc> arcs) {
        this.arcs = arcs;
    }
}
