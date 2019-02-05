package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.transfer.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BanksResponseBody {
    @JsonProperty("TransferBanks")
    private List<BankEntity> transferBanks;

    public List<BankEntity> getTransferBanks() {
        return transferBanks;
    }

    public void setTransferBanks(List<BankEntity> transferBanks) {
        this.transferBanks = transferBanks;
    }
}
