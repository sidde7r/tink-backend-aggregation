package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentsOverviewOut {

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentCount;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String hasCrossBorderPayments;

    public String getPaymentCount() {
        return paymentCount;
    }

    public void setPaymentCount(String paymentCount) {
        this.paymentCount = paymentCount;
    }

    public String getHasCrossBorderPayments() {
        return hasCrossBorderPayments;
    }
}
