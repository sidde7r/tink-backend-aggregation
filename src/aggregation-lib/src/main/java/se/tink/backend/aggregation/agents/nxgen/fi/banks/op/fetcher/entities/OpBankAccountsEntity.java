package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankAccountsEntity {

    private List<OpBankAccountEntity> accounts;

    public List<OpBankAccountEntity> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<OpBankAccountEntity> accounts) {
        this.accounts = accounts;
    }
}
