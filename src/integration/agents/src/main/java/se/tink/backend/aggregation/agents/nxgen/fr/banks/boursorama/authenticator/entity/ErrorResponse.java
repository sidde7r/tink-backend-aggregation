package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorResponse {
    private ErrorEntity error;
}
