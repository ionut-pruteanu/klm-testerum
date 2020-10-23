package klm.model;

import klm.model.context.Context;

public class FlightDetails {
    public String origin;
    public String destination;
    public String searchedFlightNumber;
    public int nrOfBookingDaysToAttemptReservation;
    public Context context;

    @Override
    public String toString() {
        return "FlightDetails{" +
            "origin='" + origin + '\'' +
            ", destination='" + destination + '\'' +
            ", searchedFlightNumber='" + searchedFlightNumber + '\'' +
            ", nrOfBookingDaysToAttemptReservation=" + nrOfBookingDaysToAttemptReservation +
            ", context=" + context +
            '}';
    }
}


