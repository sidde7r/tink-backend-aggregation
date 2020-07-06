package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private ErrorEntity error;
    private Object data;

    public ErrorEntity getError() {
        return error;
    }
}
