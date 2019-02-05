package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.entities.SignOptionsDataEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.transactionalaccount.entities.TransferDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferRequest {
    @JsonProperty("transfer_data")
    private TransferDataEntity transferData;

    @JsonProperty("sign_options_data")
    private SignOptionsDataEntity signOptionsData;

    public TransferDataEntity getTransferData() {
        return transferData;
    }

    public SignOptionsDataEntity getSignOptionsData() {
        return signOptionsData;
    }
}
