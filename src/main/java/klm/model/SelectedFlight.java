package klm.model;

public class SelectedFlight {
    public String flightNumber;
    public String shoppingCartLink;

    @Override
    public String toString() {
        return "SelectedFlight{" +
            "flightNumber='" + flightNumber + '\'' +
            ", shoppingCartLink='" + shoppingCartLink + '\'' +
            '}';
    }
}
