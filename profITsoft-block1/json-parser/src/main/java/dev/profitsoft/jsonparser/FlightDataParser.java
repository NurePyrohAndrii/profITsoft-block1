package dev.profitsoft.jsonparser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class FlightDataParser {

    private final Path directory;
    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();
    @Getter
    private final ConcurrentMap<String, Long> statistics = new ConcurrentHashMap<>();
    private final ExecutorService executorService;
    private final JsonFactory factory;
    private final int numberOfThreads;
    private final String attribute;

    public FlightDataParser(String dirPath, int numberOfThreads, String attribute) {
        this.directory = Paths.get(dirPath);
        this.numberOfThreads = numberOfThreads;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.factory = new JsonFactory();
        this.attribute = attribute;
    }

    public void process() throws IOException {
        listJsonFiles();
        processFiles();
        waitForCompletion();
        printStatistics();
    }

    private void listJsonFiles() throws IOException {
        try (Stream<Path> stream = Files.walk(directory)) {
            List<Path> files = stream.filter(path -> path.toString().endsWith(".json")).toList();
            System.out.println("Found " + files.size() + " JSON files");
            fileQueue.addAll(files);
        }
    }

    private void processFiles() {
        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                Path file;
                while ((file = fileQueue.poll()) != null) {
                    try {
                        processFile(file);
                    } catch (Exception e) {
                        System.err.println("Error processing file: " + e.getMessage());
                    }
                }
            });
        }
    }

    private void processFile(Path file) throws IOException {
        try (JsonParser parser = factory.createParser(file.toFile())) {
            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new IllegalStateException("Expected content to be an array");
            }
            while (parser.nextToken() != JsonToken.END_ARRAY) {
                if (parser.currentToken() == JsonToken.START_OBJECT) {
                    processJsonObject(parser);
                }
            }
        }
    }

    private void processJsonObject(JsonParser parser) throws IOException {
        String currentFieldName = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            JsonToken token = parser.currentToken();
            if (token == JsonToken.FIELD_NAME) {
                currentFieldName = parser.getCurrentName();
            } else if (token != null && token.isScalarValue() && currentFieldName != null && currentFieldName.equals(attribute)) {
                String value = parser.getText();
                if (value.contains(",")) {
                    String[] values = value.split(",\\s*");
                    for (String val : values) {
                        statistics.merge(val, 1L, Long::sum);
                    }
                } else {
                    statistics.merge(value, 1L, Long::sum);
                }
            }
        }
    }

    private void waitForCompletion() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private void printStatistics() {
        statistics.forEach((key, value) -> System.out.println(key + ": " + value));
    }

    public static void main(String[] args) {
        try {
            String attribute = "flightNumber";
            FlightDataParser processor = new FlightDataParser("./src/main/resources/data", 8, attribute);
            processor.process();
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}