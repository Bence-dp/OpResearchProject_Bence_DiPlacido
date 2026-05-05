package org.builder;

import org.model.Arc;
import org.model.EndNode;
import org.model.Graph;
import org.model.MiddleNode;
import org.model.Node;
import org.model.StartNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphBuilder {

    public Graph build(String filePath) throws IOException {
        return build(Path.of(filePath));
    }

    public Graph build(Path filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(filePath)) {
            String header = nextDataLine(reader);
            if (header == null) {
                throw new IllegalArgumentException("Le fichier est vide.");
            }

            String[] headerParts = header.split("\\s+");
            if (headerParts.length < 4) {
                throw new IllegalArgumentException("La première ligne doit contenir: #nodes #arcs s t");
            }

            int nodeCount = Integer.parseInt(headerParts[0]);
            int arcCount = Integer.parseInt(headerParts[1]);
            int startIndex = Integer.parseInt(headerParts[2]);
            int endIndex = Integer.parseInt(headerParts[3]);

            Map<Integer, Node> nodesByIndex = new HashMap<>();
            StartNode startNode = null;
            EndNode endNode = null;
            List<Node> middleNodes = new ArrayList<>();

            for (int index = 0; index < nodeCount; index++) {
                Node node;
                if (index == startIndex) {
                    node = new StartNode();
                    startNode = (StartNode) node;
                    node.setName("s");
                } else if (index == endIndex) {
                    node = new EndNode();
                    endNode = (EndNode) node;
                    node.setName("t");
                } else {
                    node = new MiddleNode();
                    node.setName(String.valueOf(index));
                    middleNodes.add(node);
                }

                node.setId(index);
                node.setArcsSortant(new ArrayList<>());
                nodesByIndex.put(index, node);
            }

            if (startNode == null || endNode == null) {
                throw new IllegalArgumentException("Les indices s et t doivent référencer des noeuds existants.");
            }

            int parsedArcs = 0;
            String line;
            while ((line = nextDataLine(reader)) != null) {
                String[] parts = line.split("\\s+");
                if (parts.length < 4) {
                    throw new IllegalArgumentException("Ligne d'arc invalide: " + line);
                }

                int sourceIndex = Integer.parseInt(parts[0]);
                int destinationIndex = Integer.parseInt(parts[1]);
                int capacity = Integer.parseInt(parts[2]);
                int cost = Integer.parseInt(parts[3]);
                if (capacity < 0) {
                    throw new IllegalArgumentException("Capacité négative interdite: " + line);
                }

                Node source = nodesByIndex.get(sourceIndex);
                Node destination = nodesByIndex.get(destinationIndex);
                if (source == null || destination == null) {
                    throw new IllegalArgumentException("Arc invalide: " + line);
                }

                Arc arc = new Arc();
                arc.setSource(source);
                arc.setDestination(destination);
                arc.setCapacity(capacity);
                arc.setInitialCapacity(capacity);
                arc.setFlow(0);
                arc.setCost(cost);

                source.getArcsSortant().add(arc);
                parsedArcs++;
            }

            if (parsedArcs != arcCount) {
                throw new IllegalArgumentException(
                        "Nombre d'arcs inattendu: attendu " + arcCount + ", trouvé " + parsedArcs
                );
            }

            return new Graph(startNode, endNode, middleNodes);
        }
    }

    private String nextDataLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith("#")) {
                return trimmed;
            }
        }
        return null;
    }
}
