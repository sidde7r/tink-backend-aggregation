package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChangePaymentOut {
    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String paymentSubType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String paymentSubTypeExtension;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String dueDateType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date paymentDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.String.class)
    private String statusCode;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private boolean advancedSigningRequested;

    public Payment.SubType getPaymentSubType() {
        return Payment.SubType.fromSerializedValue(paymentSubType);
    }

    public void setPaymentSubType(String paymentSubType) {
        this.paymentSubType = paymentSubType;
    }

    public Payment.SubTypeExtension getPaymentSubTypeExtension() {
        return Payment.SubTypeExtension.fromSerializedValue(paymentSubTypeExtension);
    }

    public void setPaymentSubTypeExtension(String paymentSubTypeExtension) {
        this.paymentSubTypeExtension = paymentSubTypeExtension;
    }

    public String getDueDateType() {
        return dueDateType;
    }

    public void setDueDateType(String dueDateType) {
        this.dueDateType = dueDateType;
    }

    public Date getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(Date paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Payment.StatusCode getStatusCode() {
        return Payment.StatusCode.fromSerializedValue(statusCode);
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public boolean isAdvancedSigningRequested() {
        return advancedSigningRequested;
    }

    public void setAdvancedSigningRequested(boolean advancedSigningRequested) {
        this.advancedSigningRequested = advancedSigningRequested;
    }
}
