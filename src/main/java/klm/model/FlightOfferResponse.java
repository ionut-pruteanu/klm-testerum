package klm.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FlightOfferResponse {
    private final String flightNumber;

    @JsonCreator
    public FlightOfferResponse(@JsonProperty("flightNumber") String flightNumber) {
        this.flightNumber = flightNumber;
    }

    public String getFlightNumber() {
        return flightNumber;
    }
}
