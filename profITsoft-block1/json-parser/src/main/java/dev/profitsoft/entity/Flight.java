package dev.profitsoft.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Representing of a flight entity.
 */
@Data
@AllArgsConstructor
public class Flight {

    private String flightNumber;
    private String departure;
    private String destination;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String services;

    /**
     * List of available services on a flight.
     */
    public static final List<String> AVAILABLE_SERVICES = List.of(
            "Business Class",
            "Economy Class",
            "Premium Economy",
            "First Class",
            "Wi-Fi",
            "Entertainment",
            "Meals",
            "Extra Legroom",
            "Priority Boarding",
            "In-flight Shopping"
    );
}
