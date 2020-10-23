package klm;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.testerum_api.testerum_steps_api.annotations.steps.Param;
import com.testerum_api.testerum_steps_api.annotations.steps.When;
import com.testerum_api.testerum_steps_api.services.TesterumServiceLocator;
import com.testerum_api.testerum_steps_api.test_context.logger.TesterumLogger;
import com.testerum_api.testerum_steps_api.test_context.test_vars.TestVariables;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import klm.model.FlightDetails;
import klm.model.SelectedFlight;
import klm.model.http_response.FlightOfferResponse;
import klm.model.http_response.Itinerary;
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
    @When(value = "I select the flight <<flightDetails>>",
        description = ""
            + "Search and selects the best flight based on the provided details.\n"
            + "If the flight is not available for today it will search the flight for the next days specified by `nrOfBookingDaysToAttemptReservation` parameter.\n"
            + "This step will select only direct flights.\n"
            + "\n"
            + "Step Response:\n"
            + "```\n"
            + "SELECTED_FLIGHT\n"
            + "    flightNumber: String\n"
            + "    shoppingCartLink: String\n"
            + "\n"
            + "```")
    public void selectMatchingFlightOffers(@Param FlightDetails flightDetails) throws Exception {

        Itinerary selectedFlightOffer = null;

        for (int reservationAttempts = 0; reservationAttempts < flightDetails.nrOfBookingDaysToAttemptReservation;
            reservationAttempts++) {

            LocalDate bookingDate = LocalDate.now().plusDays(reservationAttempts);
            FlightOfferResponse flightOfferResponse = attemptFlightSelection(flightDetails, bookingDate);

            List<Itinerary> directFlights = findDirectFlight(flightOfferResponse);
            if (!directFlights.isEmpty()) {
                selectedFlightOffer = directFlights.get(0);
                break;
            }
        }

        if (selectedFlightOffer != null) {
            SelectedFlight selectedFlight = new SelectedFlight();
            selectedFlight.flightNumber = selectedFlightOffer.connections.get(0).segments.get(0).marketingFlight.number;
            selectedFlight.shoppingCartLink = selectedFlightOffer.flightProducts.get(0)._links.shoppingCart.href;

            logger.info("Selected flight: " + selectedFlight.toString());
            testVariables.set("SELECTED_FLIGHT", selectedFlight);
        } else {
            throw new AssertionError(
                "No direct flights found for the request: " + flightDetails.toString()
            );
        }
    }

    private final OkHttpClient client = new OkHttpClient();

    private List<Itinerary> findDirectFlight(FlightOfferResponse flightOfferResponse) {
        List<Itinerary> response = new ArrayList<>();

        if (flightOfferResponse == null || flightOfferResponse.itineraries == null) {
            return Collections.emptyList();
        }

        for (Itinerary itinerary : flightOfferResponse.itineraries) {
            if (itinerary.connections.isEmpty()) {
                continue;
            }

            if (itinerary.connections.get(0).segments.size() != 1) {
                continue;
            }

            response.add(itinerary);
        }

        return response;
    }

    private FlightOfferResponse attemptFlightSelection(FlightDetails flightDetails, LocalDate bookingDate)
        throws Exception {

        Request request = new Request.Builder()
            .url(flightDetails.context.baseUrl + "/travel/offers/v1/available-offers")
            .header("Authorization", "Bearer " + flightDetails.context.accessToken)
            .header("Content-Type", "application/json")
            .header("AFKL-TRAVEL-Host", "KL")
            .header("Accept-Language", "en-NL")
            .header("Accept", "application/hal+json;charset=UTF-8")

            .post(RequestBody.create(
                APPLICATION_JSON,
                getFlightOffersRequestAsJson(flightDetails, LocalDate.now())
            ))
            .build();

        try (Response response = client.newCall(request).execute()) {

            if (response.code() != 200) {
                throw new AssertionError(
                    "Get Flight Offers response is not OK (200). \n"
                        + "Received Response:" + response.toString()
                );
            }

            String responeString = response.body().string();
            System.out.println("responeString = " + responeString);
            return mapper.readValue(responeString, new TypeReference<FlightOfferResponse>() {});
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
            "        \"departureDate\" : \"" + bookingDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\",\n" +
            "        \"origin\" : {\n" +
            "            \"airport\" : {\n" +
            "                \"code\" : \"" + flightDetails.origin + "\"\n" +
            "            }\n" +
            "        },\n" +
            "        \"destination\" : {\n" +
            "            \"airport\" : {\n" +
            "                \"code\" : \"" + flightDetails.destination + "\"\n" +
            "            }\n" +
            "        }\n" +
            "    }]\n" +
            "}";
    }
}
