package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
@SuppressWarnings("UnusedDeclaration")
public class ConsentStatusResponse {

    @JsonProperty("consentStatus")
    private String status;

    @JsonIgnore
    public boolean isValid() {
        return "valid".equalsIgnoreCase(status);
    }
}
