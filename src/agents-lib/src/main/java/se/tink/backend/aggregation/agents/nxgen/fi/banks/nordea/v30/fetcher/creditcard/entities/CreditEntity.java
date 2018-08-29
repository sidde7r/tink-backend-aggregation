package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditEntity {

    @JsonProperty("masked_credit_card_number")
    private String maskedCreditCardNumber;
    @JsonProperty("credit_limit")
    private double creditLimit;
    @JsonProperty("credit_booked_balance")
    private double creditBookedBalance;
    @JsonProperty("credit_available_balance")
    private double creditAvailableBalance;
    @JsonProperty("prev_min_instalment_amount")
    private double prevMinInstalmentAmount;
    @JsonProperty("instalment_min_percent")
    private double instalmentMinPercent;

    private InvoiceEntity invoice;

    public String getMaskedCreditCardNumber() {
        return maskedCreditCardNumber;
    }

    public double getSignedBalance() {
        return -creditBookedBalance;
    }

    public double getAvailableCredit() {
        return creditAvailableBalance;
    }

    public String getReferenceNumber() {
        return invoice.getReferenceNumber();
    }
}
