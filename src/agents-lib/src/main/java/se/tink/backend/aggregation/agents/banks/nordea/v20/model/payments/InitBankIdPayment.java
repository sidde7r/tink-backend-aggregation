package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapSerializer;

public class InitBankIdPayment {

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String type;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String paymentId;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String paymentSubTypeExtension;

    public String getPaymentSubTypeExtension() {
        return paymentSubTypeExtension;
    }

    public void setPaymentSubTypeExtension(String paymentSubTypeExtension) {
        this.paymentSubTypeExtension = paymentSubTypeExtension;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
