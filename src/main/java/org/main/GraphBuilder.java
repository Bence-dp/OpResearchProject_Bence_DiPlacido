package org.main;

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
                    continue;
                }

                int sourceIndex = Integer.parseInt(parts[0]);
                int destinationIndex = Integer.parseInt(parts[1]);
                int capacity = Integer.parseInt(parts[2]);
                int cost = Integer.parseInt(parts[3]);

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

            Graph graph = new Graph(startNode, endNode, middleNodes);

            // build residual graph as a separate structure and keep mapping residualArc -> originalArc
            Map<Node, Node> origToRes = new HashMap<>();
            StartNode resStart = new StartNode();
            resStart.setName(startNode.getName());
            resStart.setArcsSortant(new ArrayList<>());
            origToRes.put(startNode, resStart);

            EndNode resEnd = new EndNode();
            resEnd.setName(endNode.getName());
            resEnd.setArcsSortant(new ArrayList<>());
            origToRes.put(endNode, resEnd);

            List<Node> resMiddle = new ArrayList<>();
            for (Node n : middleNodes) {
                Node rn = new MiddleNode();
                rn.setName(n.getName());
                rn.setArcsSortant(new ArrayList<>());
                resMiddle.add(rn);
                origToRes.put(n, rn);
            }

            Graph residual = new Graph(resStart, resEnd, resMiddle);

            Map<Arc, Arc> residualToOriginal = new HashMap<>();
            Map<Arc, Boolean> residualIsReverse = new HashMap<>();

            java.util.function.BiConsumer<Arc, Node> addResidualArcs = (origArc, resSource) -> {
                Arc f = new Arc();
                f.setSource(resSource);
                Node fDest = origToRes.get(origArc.getDestination());
                f.setDestination(fDest);
                f.setCapacity(origArc.getInitialCapacity());
                f.setInitialCapacity(origArc.getInitialCapacity());
                f.setCost(origArc.getCost());
                f.setFlow(0);
                resSource.getArcsSortant().add(f);
                residualToOriginal.put(f, origArc);
                residualIsReverse.put(f, Boolean.FALSE);

                Arc r = new Arc();
                r.setSource(fDest);
                r.setDestination(resSource);
                r.setCapacity(0);
                r.setInitialCapacity(0);
                r.setCost(-origArc.getCost());
                r.setFlow(0);
                fDest.getArcsSortant().add(r);
                residualToOriginal.put(r, origArc);
                residualIsReverse.put(r, Boolean.TRUE);
            };

            if (startNode.getArcsSortant() != null) {
                for (Arc origArc : startNode.getArcsSortant()) {
                    addResidualArcs.accept(origArc, origToRes.get(startNode));
                }
            }

            for (Node origNode : middleNodes) {
                if (origNode.getArcsSortant() == null) continue;
                for (Arc origArc : origNode.getArcsSortant()) {
                    addResidualArcs.accept(origArc, origToRes.get(origNode));
                }
            }

            graph.setResidualGraph(residual);
            graph.setResidualToOriginal(residualToOriginal);
            graph.setResidualIsReverse(residualIsReverse);

            return graph;
        }
    }

    private String nextDataLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return null;
    }
}