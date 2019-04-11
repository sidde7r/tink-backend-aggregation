package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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
