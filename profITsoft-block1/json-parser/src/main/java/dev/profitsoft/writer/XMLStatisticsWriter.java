package dev.profitsoft.writer;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import dev.profitsoft.collector.StatisticsCollector;
import lombok.RequiredArgsConstructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for writing statistics to XML file.
 * The class uses Jackson library for XML serialization.
 */
@RequiredArgsConstructor
public class XMLStatisticsWriter {

    /**
     * Path to the directory where the statistics files will be saved.
     */
    private static final Path STATISTICS_LOCATION = Paths.get("src/main/resources/");

    /**
     * Statistics collector that contains
     * the statistics to be written.
     */
    private final StatisticsCollector collector;

    /**
     * Jackson XML mapper for marshalling statistics to XML.
     */
    private final XmlMapper xmlMapper;

    /**
     * Constructor that configures the writer
     * and the statistics collector.
     *
     * @param collector statistics collector
     */
    public XMLStatisticsWriter(StatisticsCollector collector) {
        this.collector = collector;
        this.xmlMapper = new XmlMapper();
        this.xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);
    }

    /**
     * Writes the statistics to an XML file.
     * The file name is based on the attribute
     * that the statistics are grouped by.
     *
     * @param attribute attribute to group statistics by
     */
    public void writeStatistics(String attribute) {
        try {
            xmlMapper.writeValue(
                    getOutputFile(attribute),
                    getMappedCollectorStatistics()
            );
        } catch (IOException e) {
            System.out.println("Error writing statistics to file: " + e.getMessage());
        }
    }

    /**
     * Returns the statistics object based on the
     * statistics collector.
     *
     * @return statistics object
     */
    private Statistics getMappedCollectorStatistics() {
        return new Statistics(
                collector.getStatistics().entrySet().stream()
                        .sorted((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()))
                        .map(entry -> new StatisticItem(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList())
        );
    }

    /**
     * Returns the path to the output file
     * based on the attribute that the statistics
     * are grouped by.
     *
     * @param attribute attribute to group statistics by
     * @return path to the output file
     */
    private File getOutputFile(String attribute) {
        return new File(STATISTICS_LOCATION.resolve("statistics_by_" + attribute + ".xml").toString());
    }

    /**
     * Inner class for representing a statistic item.
     * Used for marshalling statistics to XML as object template.
     */
    private static class StatisticItem {
        public String value;
        public Long count;

        public StatisticItem(String value, Long count) {
            this.value = value;
            this.count = count;
        }
    }

    /**
     * Inner class for representing statistics list.
     * Used for marshalling statistics to XML as object template.
     */
    private static class Statistics {
        public List<StatisticItem> item;

        public Statistics(List<StatisticItem> item) {
            this.item = item;
        }
    }
}
