package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditEntity {
    @JsonProperty("masked_credit_card_number")
    private String maskedCreditCardNumber;

    @JsonProperty("credit_available_balance")
    private double creditAvailableBalance;

    @JsonProperty("credit_booked_balance")
    private double creditBookedBalance;

    public String getMaskedCreditCardNumber() {
        return maskedCreditCardNumber;
    }

    public double getCreditAvailableBalance() {
        return creditAvailableBalance;
    }

    public double getCreditBookedBalance() {
        return creditBookedBalance;
    }
}
