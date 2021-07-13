package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.entity.account.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListAccountsResponse {

    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList());
    }
}
