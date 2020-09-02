package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorResponse {
    private String status;
    private String message;
    private String error;
}
