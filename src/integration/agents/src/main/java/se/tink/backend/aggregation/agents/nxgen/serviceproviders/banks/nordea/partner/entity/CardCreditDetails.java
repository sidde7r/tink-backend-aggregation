package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardCreditDetails {
    @JsonProperty("credit_limit")
    private BigDecimal creditLimit;

    @JsonProperty("credit_available_balance")
    private BigDecimal availableBalance;

    @JsonProperty("masked_credit_card_number")
    private String maskedCreditCardNumber;

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public String getMaskedCreditCardNumber() {
        return maskedCreditCardNumber;
    }
}
