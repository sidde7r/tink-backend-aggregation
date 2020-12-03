package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountResponse {

    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts == null ? Collections.emptyList() : accounts;
    }
}
