package org.model;

import java.util.Collection;
import java.util.List;

public abstract class Node {
    private String name;
    private List<Arc> arcsSortant;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Arc> getArcsSortant() {
        return arcsSortant;
    }

    public void setArcsSortant(List<Arc> arcs) {
        this.arcsSortant = arcs;
    }
}
