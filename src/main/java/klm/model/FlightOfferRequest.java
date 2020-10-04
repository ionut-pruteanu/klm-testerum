package klm.model;

import java.time.LocalDate;

public class FlightOfferRequest {
    private String origin;
    private String destination;
    private String carrier;
    private LocalDate bookingDate;

    public FlightOfferRequest(String origin,
                              String destination,
                              String carrier,
                              LocalDate bookingDate) {
        this.origin = origin;
        this.destination = destination;
        this.carrier = carrier;
        this.bookingDate = bookingDate;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getCarrier() {
        return carrier;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }
}
