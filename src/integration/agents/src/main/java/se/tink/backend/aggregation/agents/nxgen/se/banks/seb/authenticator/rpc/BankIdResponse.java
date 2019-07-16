package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.authenticator.entities.NextRequestEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdResponse {
    @JsonProperty private String status;
    @JsonProperty private String autostarttoken;
    @JsonProperty private String message;
    @JsonProperty private String rfa;

    @JsonProperty("next_request")
    private NextRequestEntity nextRequestEntity;

    @JsonIgnore
    public String getAutostarttoken() {
        return autostarttoken;
    }

    @JsonIgnore
    public NextRequestEntity getNextRequestEntity() {
        return nextRequestEntity;
    }

    @JsonIgnore
    public String getRfa() {
        return rfa;
    }
}
