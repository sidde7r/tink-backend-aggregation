package se.tink.backend.aggregation.agents.banks.nordea.v20.model.transfers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateTransferOut {

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String statusCode;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentDate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String warningText;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String exchangeRate;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String dueDateType;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String paymentToken;

    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String counterValue;

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(String paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getWarningText() {
        return warningText;
    }

    public void setWarningText(String warningText) {
        this.warningText = warningText;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getDueDateType() {
        return dueDateType;
    }

    public void setDueDateType(String dueDateType) {
        this.dueDateType = dueDateType;
    }

    public String getPaymentToken() {
        return paymentToken;
    }

    public void setPaymentToken(String paymentToken) {
        this.paymentToken = paymentToken;
    }

    public String getCounterValue() {
        return counterValue;
    }

    public void setCounterValue(String counterValue) {
        this.counterValue = counterValue;
    }
}
