package klm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testerum_api.testerum_steps_api.annotations.steps.Param;
import com.testerum_api.testerum_steps_api.annotations.steps.When;
import com.testerum_api.testerum_steps_api.services.TesterumServiceLocator;
import com.testerum_api.testerum_steps_api.test_context.logger.TesterumLogger;
import com.testerum_api.testerum_steps_api.test_context.test_vars.TestVariables;
import java.time.LocalDate;
import java.util.List;
import klm.model.FlightOfferRequest;
import klm.model.FlightOfferResponse;
import klm.model.FlightDetails;
import klm.model.FlightSearchResultVariables;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FlightOffers {
    public static final MediaType APPLICATION_JSON = MediaType.parse("application/json; charset=utf-8");

    private final TesterumLogger logger = TesterumServiceLocator.getTesterumLogger();
    private final TestVariables testVariables = TesterumServiceLocator.getTestVariables();

    private final ObjectMapper mapper = new ObjectMapper();
    private final OkHttpClient client = new OkHttpClient();

    @When(value = "I select the flight <<flightDetails>> with the results stored in the variables <<flightSearchResultVariables>>",
        description = ""
            + "Search and selects the best flight based on the provided details.\n"
            + "If the flight is not available for today it will search the flight for the next days specified by `nrOfBookingDaysToAttemptReservation` parameter")
    public void selectMatchingFlightOffers(
        @Param FlightDetails flightDetails,
        @Param FlightSearchResultVariables flightSearchResultVariables) throws Exception {

        FlightOfferResponse selectedFlightOffer = null;

        for (int reservationAttempts = 0; reservationAttempts < flightDetails.nrOfBookingDaysToAttemptReservation; reservationAttempts++) {

            LocalDate bookingDate = LocalDate.now().plusDays(reservationAttempts);
            List<FlightOfferResponse> flightOffers = attemptFlightSelection(flightDetails, bookingDate);

            if (!flightOffers.isEmpty()) {
                selectedFlightOffer = flightOffers.get(0);
                break;
            }
        }

        if (selectedFlightOffer != null) {
            logger.info("Selected flight number: " + selectedFlightOffer.getFlightNumber());
            testVariables.set(flightSearchResultVariables.flightNumberVariableName, selectedFlightOffer.getFlightNumber());
        }
    }

    private List<FlightOfferResponse> attemptFlightSelection(FlightDetails flightDetails, LocalDate bookingDate)
        throws Exception {

        Request request = new Request.Builder()
            .url(flightDetails.context.baseUrl + "/flights/offers")
            .header("Authentication", "Basic " + flightDetails.context.accessToken)
            .post(RequestBody.create(
                APPLICATION_JSON,
                getFlightOffersRequestAsJson(flightDetails, LocalDate.now())
            ))
            .build();

        try (Response response = client.newCall(request).execute()) {
            return mapper.readValue(response.body().string(), new TypeReference<List<FlightOfferResponse>>() {});
        }
    }

    private String getFlightOffersRequestAsJson(FlightDetails flightDetails, LocalDate bookingDate) throws Exception {
        FlightOfferRequest request = new FlightOfferRequest(
            flightDetails.origin,
            flightDetails.destination,
            flightDetails.carrier,
            bookingDate
        );
        return mapper.writeValueAsString(request);
    }
}
