package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeletePaymentsOut {

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentDeleted;

    public String getPaymentDeleted() {
        return paymentDeleted;
    }

    public void setPaymentDeleted(String paymentDeleted) {
        this.paymentDeleted = paymentDeleted;
    }
}
