package dev.profitsoft.parser;

import dev.profitsoft.collector.StatisticsCollector;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;

class ParsePerformanceTest {

    private static final String TEST_DATA_DIRECTORY
            = Paths.get("src/main/resources/data").toString();
    private static final String TEST_PROPERTY = "services";

    @ParameterizedTest
    @ValueSource(shorts = { 1, 1, 2, 4, 8 })
    void measureParseTime(int threadNumber) throws IOException {
        long parseTime = 0;
        for (int k = 0; k < 5; k++) {
            var parser = new FlightDataParser(TEST_DATA_DIRECTORY, threadNumber, TEST_PROPERTY, new StatisticsCollector());
            LocalDateTime startTime = LocalDateTime.now();
            parser.parse();
            LocalDateTime finishTime = LocalDateTime.now();
            parseTime += Duration.between(startTime, finishTime).toMillis();
            System.gc();
        }
        System.out.printf("Parsing duration %d milliseconds with thread number %d \n", parseTime / 5, threadNumber);
    }

}
