package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.abnamro.authenticator.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ErrorResponse {
    private List<Errors> errors;
}
