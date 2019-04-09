package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentDetailsResponse {

    @JsonProperty("getPaymentDetailsOut")
    private PaymentDetailsResponseOut paymentDetailsResponseOut;

    public PaymentDetailsResponseOut getPaymentDetailsResponseOut() {
        return paymentDetailsResponseOut;
    }

    public void setPaymentDetailsResponseOut(PaymentDetailsResponseOut paymentDetailsResponseOut) {
        this.paymentDetailsResponseOut = paymentDetailsResponseOut;
    }

    public String getPaymentId() {
        if (paymentDetailsResponseOut == null) {
            return null;
        }
        return paymentDetailsResponseOut.getPaymentId();
    }
}
