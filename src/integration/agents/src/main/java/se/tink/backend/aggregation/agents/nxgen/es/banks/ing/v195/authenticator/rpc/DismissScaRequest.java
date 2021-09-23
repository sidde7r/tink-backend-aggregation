package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import lombok.Value;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Value
public final class DismissScaRequest {
    String personId;
    int channel = 83;
    String reason = "Rooted mobile";

    public DismissScaRequest(String personId) {
        this.personId = personId;
    }
}
