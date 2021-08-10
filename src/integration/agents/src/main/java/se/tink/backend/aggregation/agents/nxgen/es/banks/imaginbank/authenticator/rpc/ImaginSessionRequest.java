package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@JsonObject
public class ImaginSessionRequest {
    private String ima;
    private SessionRequest reqImagin;
}
