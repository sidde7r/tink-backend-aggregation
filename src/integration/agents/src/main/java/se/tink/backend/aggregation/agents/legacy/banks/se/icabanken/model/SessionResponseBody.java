package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionResponseBody {
    @JsonProperty("Customer")
    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public boolean mustUpdateInformationToICABanken() {
        if (customer != null && customer.getKnowYourCustomer() != null) {
            return customer.getKnowYourCustomer().isMustUpdate();
        }
        return false;
    }
}
