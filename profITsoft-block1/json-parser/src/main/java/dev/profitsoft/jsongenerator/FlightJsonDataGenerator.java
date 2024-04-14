package dev.profitsoft.jsongenerator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.javafaker.Faker;
import dev.profitsoft.entity.Flight;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static dev.profitsoft.entity.Flight.AVAILABLE_SERVICES;

/**
 * <p>An object that generates JSON files with random flight data.</p>
 * <p>{@code FlightJsonDataGenerator} defines the method {@link #generateFilesWithFlights(int, int)
 * generateFilesWithFlights} that generates a specified number of JSON files with random flight data.</p>
 */
public class FlightJsonDataGenerator {

    /**
     * The location of the directory where the generated files will be stored.
     */
    private static final Path DATA_LOCATION = Paths.get("src/main/resources/data");

    /**
     * The name of the generated files.
     */
    private static final String DATA_FILE_NAME = "flights";

    /**
     * The extension of the generated files.
     */
    private static final String DATA_FILE_EXTENSION = ".json";

    /**
     * The {@link Faker} object used to generate random data.
     * <p>It is initialized with the English locale.</p>
     */
    private final static Faker faker = new Faker(new Locale("en"));

    /**
     * The {@link ObjectMapper} object used to write JSON data.
     */
    private final ObjectMapper mapper;

    /**
     * Constructs a new {@code FlightJsonDataGenerator} object.
     * <p>Initializes the {@link ObjectMapper} object with the {@link JavaTimeModule} module and the
     * {@link SerializationFeature#WRITE_DATES_AS_TIMESTAMPS} feature set to {@code false} to write
     * dates as strings in the ISO-8601 format.</p>
     */
    public FlightJsonDataGenerator() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * <p>Generates a specified number of JSON files with random flight data of a specified number of flights in each file.</p>
     *
     * @param numberOfFiles          the number of files to generate
     * @param numberOfFlightsPerFile the number of flights to generate in each file
     */
    public void generateFilesWithFlights(int numberOfFiles, int numberOfFlightsPerFile) {
        for (int i = 0; i < numberOfFiles; i++) {
            createDataLocationDirectory();
            String currentFileName = DATA_FILE_NAME + i;
            generateFileWithFlights(buildDataFilePath(currentFileName), numberOfFlightsPerFile);
        }
    }

    /**
     * Generates a JSON file with random flight data.
     *
     * @param file            the path to the file to generate
     * @param numberOfFlights the number of flights to generate
     */
    private void generateFileWithFlights(Path file, int numberOfFlights) {
        try (BufferedWriter writer = Files.newBufferedWriter(file)) {
            try (Stream<Flight> flightStream = Stream.generate(FlightJsonDataGenerator::generateRandomFlight).limit(numberOfFlights)) {
                mapper.writeValue(writer, flightStream.toArray(Flight[]::new));
            }
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    /**
     * Creates the directory where the generated files will be stored.
     */
    private void createDataLocationDirectory() {
        try {
            Files.createDirectories(DATA_LOCATION);
        } catch (IOException e) {
            System.err.println("Error creating directory: " + e.getMessage());
        }
    }

    /**
     * Builds the path to the file with the specified name.
     *
     * @param filename the name of the file
     * @return the path to the file
     */
    private Path buildDataFilePath(String filename) {
        return DATA_LOCATION.resolve(filename + DATA_FILE_EXTENSION);
    }

    /**
     * Generates a random flight.
     *
     * @return the generated flight
     * @see Flight
     */
    private static Flight generateRandomFlight() {
        return new Flight(faker.regexify("[A-Z]{2}[0-9]{3}"), faker.regexify("[A-Z]{3}"), faker.regexify("[A-Z]{3}"), LocalDateTime.now().plusMinutes(faker.number().numberBetween(1, 1000)), LocalDateTime.now().plusMinutes(faker.number().numberBetween(1001, 2000)), getRandomFlightServices());
    }

    /**
     * Generates a random string with a random number of services from the list of available services.
     * <p>The number of services is between 2 and the total number of available services.</p>
     *
     * @return the generated string
     */
    private static String getRandomFlightServices() {
        List<String> services = new ArrayList<>(AVAILABLE_SERVICES);
        Collections.shuffle(services);
        return String.join(", ", AVAILABLE_SERVICES.subList(0, faker.number().numberBetween(2, AVAILABLE_SERVICES.size())));
    }

    public static void main(String[] args) {
        FlightJsonDataGenerator generator = new FlightJsonDataGenerator();
        generator.generateFilesWithFlights(5, 10);
    }
}
