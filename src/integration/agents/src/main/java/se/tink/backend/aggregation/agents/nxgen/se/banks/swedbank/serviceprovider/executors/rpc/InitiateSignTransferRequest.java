package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.executors.rpc;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
public class InitiateSignTransferRequest {
    private final boolean initiate;

    public static InitiateSignTransferRequest create() {
        return new InitiateSignTransferRequest(true);
    }
}
