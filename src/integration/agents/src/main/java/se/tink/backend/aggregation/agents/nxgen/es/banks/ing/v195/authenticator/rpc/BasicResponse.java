package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BasicResponse {
    @Getter private int responseCode;

    public boolean isFail() {
        return responseCode != 0;
    }
}
