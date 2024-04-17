package dev.profitsoft.collector;

import lombok.Getter;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

/**
 * Class to collect and print statistics of consumed values
 */
@Getter
public class StatisticsCollector implements Consumer<String> {

    /**
     * Map to store statistics of consumed values
     */
    private final ConcurrentMap<String, Long> statistics = new ConcurrentHashMap<>();

    /**
     * Method to accept a value and update the statistics
     *
     * @param value the value to be consumed
     */
    @Override
    public void accept(String value) {
        if (value.contains(",")) {
            String[] values = value.split(",");
            for (String val : values) {
                statistics.merge(val.trim(), 1L, Long::sum);
            }
        } else {
            statistics.merge(value, 1L, Long::sum);
        }
    }
}
