package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import java.util.List;

public class BankListResponse {
    private List<BankEntity> allBankNames;

    public List<BankEntity> getAllBankNames() {
        return allBankNames;
    }

    public void setAllBankNames(List<BankEntity> allBankNames) {
        this.allBankNames = allBankNames;
    }
}
