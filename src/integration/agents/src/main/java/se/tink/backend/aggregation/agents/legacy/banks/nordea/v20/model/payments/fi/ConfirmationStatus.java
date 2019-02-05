package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.fi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmationStatus {

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String date;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentId;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentSubType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String statusCode;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String statusExtensionCode;

    public String getStatusExtensionCode() {
        return statusExtensionCode;
    }

    public void setStatusExtensionCode(String statusExtensionCode) {
        this.statusExtensionCode = statusExtensionCode;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getPaymentSubType() {
        return paymentSubType;
    }

    public void setPaymentSubType(String paymentSubType) {
        this.paymentSubType = paymentSubType;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
