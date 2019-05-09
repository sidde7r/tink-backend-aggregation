package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountResponse {

    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccountList() {
        return accounts;
    }

    public List<TransactionalAccount> toTinkAccounts(String owner) {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .map(account -> account.toTinkAccount(owner))
                .collect(Collectors.toList());
    }
}
