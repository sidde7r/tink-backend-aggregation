package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class JweRequest {
    private String data;
    private String type;
}
