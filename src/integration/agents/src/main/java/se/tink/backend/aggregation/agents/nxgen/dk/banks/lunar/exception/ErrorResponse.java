package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.exception;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorResponse {
    private String reasonCode;
    private String reasonDisplayMessage;
}
