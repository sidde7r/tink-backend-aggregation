package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.entity.OpBankAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankAccountsResponse {

    private List<OpBankAccountEntity> accounts;

    public List<OpBankAccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<OpBankAccountEntity> accounts) {
        this.accounts = accounts;
    }
}
