package org.model;

import java.util.List;

public abstract class Node {
    private int id;
    private String name;
    private List<Arc> arcsSortant;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
