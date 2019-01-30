package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

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
