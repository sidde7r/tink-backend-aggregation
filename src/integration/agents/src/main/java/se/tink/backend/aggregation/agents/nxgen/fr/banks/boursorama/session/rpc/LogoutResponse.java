package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.session.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class LogoutResponse {
    private boolean success;
}
