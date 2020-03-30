package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VerifySignatureResponse {
    @JsonProperty("operacionFirmada")
    private boolean operationSigned;

    public boolean isOperationSigned() {
        return operationSigned;
    }
}
