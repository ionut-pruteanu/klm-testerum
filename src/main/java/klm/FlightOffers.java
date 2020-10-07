package klm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testerum_api.testerum_steps_api.annotations.steps.Param;
import com.testerum_api.testerum_steps_api.annotations.steps.When;
import com.testerum_api.testerum_steps_api.services.TesterumServiceLocator;
import com.testerum_api.testerum_steps_api.test_context.logger.TesterumLogger;
import com.testerum_api.testerum_steps_api.test_context.test_vars.TestVariables;
import klm.model.FlightDetails;
import klm.model.FlightOfferResponse;
import klm.model.FlightSearchResultVariables;
import okhttp3.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
            .url(flightDetails.context.baseUrl + "/travel/offers/v1/available-offers")
            .header("Authorization", "Bearer " + flightDetails.context.accessToken)
            .header("Content-Type","application/json")
            .header("AFKL-TRAVEL-Host","KL")
            .header("Accept-Language","en-NL")
            .header("Accept","application/hal+json;charset=UTF-8")

            .post(RequestBody.create(
                APPLICATION_JSON,
                getFlightOffersRequestAsJson(flightDetails, LocalDate.now())
            ))
            .build();

        try (Response response = client.newCall(request).execute()) {
            String responeString = response.body().string();
            System.out.println("responeString = " + responeString);
            return mapper.readValue(responeString, new TypeReference<List<FlightOfferResponse>>() {});
        }
    }

    private String getFlightOffersRequestAsJson(FlightDetails flightDetails, LocalDate bookingDate) throws Exception {
        return "{\n" +
                "    \"commercialCabins\" : [ \"ECONOMY\" ],\n" +
                "    \"minimumAccuracy\" : 100.0,\n" +
                "    \"bookingFlow\" : \"LEISURE\",\n" +
                "    \"passengers\" : [  {\n" +
                "            \"id\": 1,\n" +
                "            \"type\": \"ADT\",\n" +
                "            \"minAge\": 25\n" +
                "        }\n" +
                "    ],\n" +
                "    \"requestedConnections\" : [ {\n" +
                "        \"departureDate\" : \""+bookingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))+"\",\n" +
                "        \"origin\" : {\n" +
                "            \"airport\" : {\n" +
                "                \"code\" : \""+flightDetails.origin+"\"\n" +
                "            }\n" +
                "        },\n" +
                "        \"destination\" : {\n" +
                "            \"airport\" : {\n" +
                "                \"code\" : \""+flightDetails.destination+"\"\n" +
                "            }\n" +
                "        }\n" +
                "    }]\n" +
                "}";
    }
}
