package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher.transactional.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}
