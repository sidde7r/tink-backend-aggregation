package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.account;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDataEntity {
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
