package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ModeEntity {
    @JsonProperty("paymentModeCode")
    private int paymentModeCode;

    @JsonProperty("formapagotarjeta")
    private String cardPaymentForm;

    public int getPaymentModeCode() {
        return paymentModeCode;
    }

    public String getCardPaymentForm() {
        return cardPaymentForm;
    }
}
