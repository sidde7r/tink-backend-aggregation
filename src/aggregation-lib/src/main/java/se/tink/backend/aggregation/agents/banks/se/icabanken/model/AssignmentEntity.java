package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AssignmentEntity {
    @JsonProperty("Amount")
    private Double amount;
    @JsonProperty("RegistrationId")
    private String registrationId;
    @JsonProperty("IsRejected")
    private boolean rejected;

    public Double getAmount() {
        return amount;
    }

    public String getRegistrationId() {
        return registrationId;
    }

    public boolean isRejected() {
        return rejected;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public void setRejected(boolean rejected) {
        this.rejected = rejected;
    }

}
