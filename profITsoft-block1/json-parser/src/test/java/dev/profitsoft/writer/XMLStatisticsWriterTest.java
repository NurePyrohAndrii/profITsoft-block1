package dev.profitsoft.writer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dev.profitsoft.collector.StatisticsCollector;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class XMLStatisticsWriterTest {

    @Mock
    private StatisticsCollector mockCollector;

    @Mock
    private XmlMapper xmlMapper;

    private XMLStatisticsWriter statisticsWriter;

    @TempDir
    Path tempDirectory;

    @BeforeEach
    void setUp() {
        // Given
        tempDirectory = Paths.get("src/main/resources/");

        MockitoAnnotations.openMocks(this);
        statisticsWriter = new XMLStatisticsWriter(mockCollector);
    }

    @AfterAll
    static void cleanUp() {
        File directory = new File("src/main/resources/");

        for (File file : Objects.requireNonNull(directory.listFiles())) {
            if (file.getName().startsWith("statistics_by_")) {
                file.delete();
            }
        }
    }


    @Test
    void testWriteStatistics_CreatesFileWithCorrectName() {
        // Given
        String attribute = "testAttribute";
        ConcurrentMap<String, Long> fakeStats = new ConcurrentHashMap<>();
        fakeStats.put("testValue", 100L);
        when(mockCollector.getStatistics()).thenReturn(fakeStats);

        // When
        statisticsWriter.writeStatistics(attribute);

        // Then
        File expectedFile = tempDirectory.resolve("statistics_by_" + attribute + ".xml").toFile();
        assertTrue(expectedFile.exists());
    }

    @Test
    void testWriteStatistics_HandlesIOException() throws IOException {
        // Given
        doThrow(new IOException("Test exception")).when(xmlMapper).writeValue(any(File.class), any());
        when(mockCollector.getStatistics()).thenReturn(new ConcurrentHashMap<>());

        // When & Then
        assertThrows(IOException.class, () -> xmlMapper.writeValue(new File("test.xml"), new Object()));
        assertDoesNotThrow(() -> statisticsWriter.writeStatistics("testAttribute"));
    }

    @Test
    void testWriteStatistics_ValidContentWritten() throws IOException {
        // Given
        String attribute = "value";
        ConcurrentMap<String, Long> fakeStats = new ConcurrentHashMap<>();
        fakeStats.put("ExampleValue", 123L);
        when(mockCollector.getStatistics()).thenReturn(fakeStats);

        // When
        statisticsWriter.writeStatistics(attribute);

        // Then
        File expectedFile = tempDirectory.resolve("statistics_by_" + attribute + ".xml").toFile();
        assertTrue(expectedFile.exists());

        String fileContents = readFileContents(expectedFile);
        assertTrue(fileContents.contains("<value>ExampleValue</value>"));
        assertTrue(fileContents.contains("<count>123</count>"));
    }

    @Test
    void testEmptyStatistics() throws IOException {
        // Given
        String attribute = "empty";
        when(mockCollector.getStatistics()).thenReturn(new ConcurrentHashMap<>());

        // When
        statisticsWriter.writeStatistics(attribute);

        // Then
        File expectedFile = tempDirectory.resolve("statistics_by_" + attribute + ".xml").toFile();
        assertTrue(expectedFile.exists());

        String fileContents = readFileContents(expectedFile);
        assertTrue(fileContents.contains("</Statistics>"));
        assertFalse(fileContents.contains("<value>"));
    }

    @Test
    void testSortedOrderOfStatistics() throws IOException {
        // Given
        String attribute = "sorted";
        ConcurrentMap<String, Long> stats = new ConcurrentHashMap<>();
        stats.put("A", 300L);
        stats.put("B", 200L);
        stats.put("C", 100L);
        when(mockCollector.getStatistics()).thenReturn(stats);

        // When
        statisticsWriter.writeStatistics(attribute);

        // Then
        File expectedFile = tempDirectory.resolve("statistics_by_" + attribute + ".xml").toFile();
        assertTrue(expectedFile.exists());

        String fileContents = readFileContents(expectedFile);
        assertTrue(fileContents.indexOf("<value>A</value>") < fileContents.indexOf("<value>B</value>"));
        assertTrue(fileContents.indexOf("<value>B</value>") < fileContents.indexOf("<value>C</value>"));
    }

    @Test
    void testFileLocationResolution() {
        // When
        String attribute = "location";

        // When
        File expectedFile = statisticsWriter.getOutputFile(attribute);

        // Then
        assertEquals(expectedFile.getPath(), tempDirectory.resolve("statistics_by_" + attribute + ".xml").toString());
    }


    private String readFileContents(File file) throws IOException {
        return new String(Files.readAllBytes(file.toPath()));
    }
}