package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.LoanAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoanAccountsResponse {

    private List<LoanAccountEntity> accounts;
    private LoanAccountEntity defaultAccount;

    public List<LoanAccountEntity> getAccounts() {
        return accounts;
    }

    public LoanAccountEntity getDefaultAccount() {
        return defaultAccount;
    }
}
