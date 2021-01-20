package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class Errors {
    private String code;
    private String message;
    private String reference;
    private String traceId;
    private String status;
    private String category;
}
