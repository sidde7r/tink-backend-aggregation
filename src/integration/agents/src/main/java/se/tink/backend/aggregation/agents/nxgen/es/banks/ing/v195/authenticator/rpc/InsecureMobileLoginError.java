package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import lombok.Value;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Value
public class InsecureMobileLoginError {
    String personId;
    String pluginId = "mfv.register";
    String errorMessage = "Error MFV: activation in enrollment flow";
    String errorText = "Risky  client";
    String errorCode = "36";

    public InsecureMobileLoginError(String personId) {
        this.personId = personId;
    }
}
