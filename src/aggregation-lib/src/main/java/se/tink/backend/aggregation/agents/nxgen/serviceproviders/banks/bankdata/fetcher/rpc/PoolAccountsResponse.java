package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataPoolAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PoolAccountsResponse {
    private List<BankdataPoolAccountEntity> poolAccounts;

    public List<BankdataPoolAccountEntity> getPoolAccounts() {
        return poolAccounts;
    }
}
