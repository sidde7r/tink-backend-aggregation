package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountFetchingResponse {
    private List<AccountEntity> list;

    public List<TransactionalAccount> toTinkAccounts() {
        if (this.list == null || this.list.isEmpty()) {
            return Collections.emptyList();
        }
        return this.list.stream()
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}

