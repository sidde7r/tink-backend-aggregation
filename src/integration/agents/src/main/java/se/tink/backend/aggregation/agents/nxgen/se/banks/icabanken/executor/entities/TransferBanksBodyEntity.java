package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.executor.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransferBanksBodyEntity {
    @JsonProperty("TransferBanks")
    private List<TransferBankEntity> transferBanks;

    public List<TransferBankEntity> getTransferBanks() {
        return transferBanks;
    }
}
