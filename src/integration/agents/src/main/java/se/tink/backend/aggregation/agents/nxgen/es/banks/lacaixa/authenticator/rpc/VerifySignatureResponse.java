package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class VerifySignatureResponse {
    @JsonProperty("operacionFirmada")
    private boolean operationSigned;
}
