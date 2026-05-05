package org.main;

import org.model.Graph;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DotPdfExporter {
    private static final Path RESOURCES_DIRECTORY = Path.of("src/main/resources");

    public void export(Graph graph, Path inputFilePath, String outputDirectoryName, int cost, int maxFlow) throws IOException {
        String baseName = baseName(inputFilePath);
        Path outputDirectory = RESOURCES_DIRECTORY.resolve(outputDirectoryName);
        Files.createDirectories(outputDirectory);

        Path dotPath = outputDirectory.resolve(baseName + ".dot");
        Path pdfPath = outputDirectory.resolve(baseName + ".pdf");

        Files.writeString(dotPath, addGraphInfo(graph.toString(), cost, maxFlow), StandardCharsets.UTF_8);
        generatePdf(dotPath, pdfPath);
    }

    private String addGraphInfo(String dot, int cost, int maxFlow) {
        String info = "label=\"Cost: " + cost + "\\nMax flow: " + maxFlow + "\";\nlabelloc=t;\n";
        int graphOptionsEnd = dot.indexOf("]\n");
        if (graphOptionsEnd == -1) {
            return dot;
        }
        int insertIndex = graphOptionsEnd + 2;
        return dot.substring(0, insertIndex) + info + dot.substring(insertIndex);
    }

    private void generatePdf(Path dotPath, Path pdfPath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "dot",
                "-Tpdf",
                dotPath.toString(),
                "-o",
                pdfPath.toString()
        );
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("Graphviz dot a échoué: " + output.trim());
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IOException("Génération PDF interrompue", exception);
        }
    }

    private String baseName(Path inputFilePath) {
        String fileName = inputFilePath.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex <= 0) {
            return fileName;
        }
        return fileName.substring(0, lastDotIndex);
    }
}
