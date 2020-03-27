package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaOptionsEncryptedPayload {

    @JsonProperty private List<String> secondFactorOptions;

    public List<String> getSecondFactorOptions() {
        return secondFactorOptions;
    }
}
