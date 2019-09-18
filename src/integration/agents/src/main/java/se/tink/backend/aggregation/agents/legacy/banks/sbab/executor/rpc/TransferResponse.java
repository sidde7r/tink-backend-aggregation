package se.tink.backend.aggregation.agents.banks.sbab.executor.rpc;

import se.tink.backend.aggregation.agents.banks.sbab.executor.entities.TransferEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferResponse {
    private TransferEntity transfer;
    private boolean needSigning;

    public TransferEntity getTransfer() {
        return transfer;
    }
}
