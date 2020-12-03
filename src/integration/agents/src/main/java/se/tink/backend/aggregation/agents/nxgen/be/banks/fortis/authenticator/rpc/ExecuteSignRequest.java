package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.rpc;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@RequiredArgsConstructor
public class ExecuteSignRequest {
    private final String requestedMeanId;
}
