package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentPoolAccountsResponse {
    private List<Object> poolAccounts;

    public List<Object> getPoolAccounts() {
        return poolAccounts;
    }
}
