package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.executors.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitiateSignTransferRequest {
    private final boolean initiate;

    public InitiateSignTransferRequest(boolean initiate) {
        this.initiate = initiate;
    }

    public static InitiateSignTransferRequest create() {
        return new InitiateSignTransferRequest(true);
    }
}
