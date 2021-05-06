package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorResponse {
    private Integer code;
}
