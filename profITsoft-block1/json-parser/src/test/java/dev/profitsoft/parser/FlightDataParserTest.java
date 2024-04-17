package dev.profitsoft.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FlightDataParserTest {

    @TempDir
    Path tempDirectory;

    @Mock
    private Consumer<String> valueConsumer;

    private FlightDataParser flightDataParser;

    @BeforeEach
    public void setUp() throws IOException {
        // Given
        Files.writeString(tempDirectory.resolve("flight1.json"), "[{\"flightNumber\":\"12345\"}]");
        Files.writeString(tempDirectory.resolve("flight2.json"), "[{\"flightNumber\":\"67890\"}]");

        flightDataParser = new FlightDataParser(tempDirectory.toString(), 2, "flightNumber", valueConsumer);
    }

    @Test
    public void testParseFiles() throws Exception {
        // When
        flightDataParser.parse();

        // Then
        verify(valueConsumer, times(1)).accept("12345");
        verify(valueConsumer, times(1)).accept("67890");
    }

    @Test
    public void testProcessFile_withInvalidJson() throws Exception {
        // Given
        Files.writeString(tempDirectory.resolve("flight3.json"), "invalid json");

        // When & Then
        assertThrows(IOException.class, () -> flightDataParser.processFile(tempDirectory.resolve("flight3.json")));

    }

    @Test
    public void testNoJsonFilesFound() throws IOException {
        // Given
        Files.walk(tempDirectory).filter(Files::isRegularFile).forEach(path -> {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // When
        flightDataParser.parse();

        // Then
        verify(valueConsumer, never()).accept(anyString());
    }

    @Test
    public void testProcessFile_withUnexpectedJsonStructure() throws Exception {
        // Given
        Files.writeString(tempDirectory.resolve("flight4.json"), "\"flightNumber\":\"12345\"}");

        assertThrows(IllegalStateException.class, () -> flightDataParser.processFile(tempDirectory.resolve("flight4.json")));
    }

    @Test
    public void testShutdownOnCompletion() throws Exception {
        // When
        flightDataParser.parse();

        // Then
        assertTrue(((ExecutorService) getExecutorService(flightDataParser)).isShutdown());
    }

    private Object getExecutorService(Object object) throws Exception {
        Field field = object.getClass().getDeclaredField("executorService");
        field.setAccessible(true);
        return field.get(object);
    }
}
