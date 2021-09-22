package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InsecureMobileLoginError {
    private String personId;
    private String pluginId = "mfv.register";
    private String errorMessage = "Error MFV: activation in enrollment flow";
    private String errorText = "Risky  client";
    private String errorCode = "36";

    public static InsecureMobileLoginError create(String personId) {
        InsecureMobileLoginError insecureMobileLoginError = new InsecureMobileLoginError();
        insecureMobileLoginError.personId = personId;
        return insecureMobileLoginError;
    }
}
