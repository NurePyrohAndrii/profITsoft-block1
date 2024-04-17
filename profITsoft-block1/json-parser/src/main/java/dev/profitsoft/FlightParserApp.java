package dev.profitsoft;

import dev.profitsoft.collector.StatisticsCollector;
import dev.profitsoft.parser.FlightDataParser;
import dev.profitsoft.writer.XMLStatisticsWriter;

import java.io.IOException;

/**
 * The main class of the application.
 * Coordinates the work of the parser and writer.
 */
public class FlightParserApp {
    public static void main(String[] args) throws IOException {

        if (args.length < 2) {
            System.err.println("Use: java -jar yourprogram.jar <dirPath> <attribute>");
            System.exit(1);
        }

        String dirPath = args[0];
        String attribute = args[1];

        StatisticsCollector collector = new StatisticsCollector();

        new FlightDataParser(dirPath, 4, attribute, collector).parse();
        new XMLStatisticsWriter(collector).writeStatistics(attribute);

    }
}