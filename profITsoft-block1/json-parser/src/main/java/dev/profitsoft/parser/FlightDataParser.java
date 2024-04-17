package dev.profitsoft.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Class to parse JSON files containing flight data
 */
public class FlightDataParser {

    /**
     * Path to the directory containing JSON files
     */
    private final Path directory;

    /**
     * Queue to store paths to JSON files to be processed
     */
    private final BlockingQueue<Path> fileQueue = new LinkedBlockingQueue<>();

    /**
     * Executor service to manage threads
     */
    private final ExecutorService executorService;

    /**
     * JSON factory to create JSON parsers
     */
    private final JsonFactory factory;

    /**
     * Number of threads to use for processing files
     */
    private final int numberOfThreads;

    /**
     * Attribute to extract from JSON objects
     */
    private final String attribute;

    /**
     * Consumer to process extracted attribute values
     */
    private final Consumer<String> valueConsumer;

    /**
     * Constructor, configures the parser with the necessary parameters
     *
     * @param dirPath         path to the directory containing JSON files
     * @param numberOfThreads number of threads to use for processing files
     * @param attribute       attribute to extract from JSON objects
     * @param valueConsumer   consumer to process extracted attribute values
     */
    public FlightDataParser(String dirPath, int numberOfThreads, String attribute, Consumer<String> valueConsumer) {
        this.directory = Paths.get(dirPath);
        this.numberOfThreads = numberOfThreads;
        this.executorService = Executors.newFixedThreadPool(numberOfThreads);
        this.factory = new JsonFactory();
        this.attribute = attribute;
        this.valueConsumer = valueConsumer;
    }

    /**
     * Method to process all JSON files in the given directory
     *
     * @throws IOException if an I/O error occurs
     */
    public void parse() throws IOException {
        listJsonFiles();
        processFiles();
        waitForCompletion();
    }

    /**
     * Method to list all JSON files in the given directory
     *
     * @throws IOException if an I/O error occurs
     */
    private void listJsonFiles() throws IOException {
        try (Stream<Path> stream = Files.walk(directory)) {
            List<Path> files = stream.filter(path -> path.toString().endsWith(".json")).toList();
            fileQueue.addAll(files);
        }
    }

    /**
     * Method to process JSON files using the executor service
     */
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

    /**
     * Method to process a JSON file
     *
     * @param file path to the JSON file
     * @throws IOException if an I/O error occurs
     */
    void processFile(Path file) throws IOException {
        try (
                BufferedReader reader = Files.newBufferedReader(file);
                JsonParser parser = factory.createParser(reader)
        ) {
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

    /**
     * Method to process a JSON object, delegating the
     * processing of the attribute value to the consumer
     *
     * @param parser JSON parser
     * @throws IOException if an I/O error occurs
     */
    private void processJsonObject(JsonParser parser) throws IOException {
        String currentFieldName = null;
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            JsonToken token = parser.currentToken();
            if (token == JsonToken.FIELD_NAME) {
                currentFieldName = parser.getCurrentName();
            } else if (
                    token != null && currentFieldName != null && currentFieldName.equals(attribute)
            ) {
                String value = parser.getText();
                valueConsumer.accept(value);
            }
        }
    }

    /**
     * Method to wait for the completion of all threads in the executor service
     */
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
}