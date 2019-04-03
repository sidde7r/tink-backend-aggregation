package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class GetAccountsResponse {

    private List<AccountEntity> accounts;

    public List<TransactionalAccount> toTinkAccounts() {
        return accounts != null
                ? accounts.stream().map(AccountEntity::toTinkAccount).collect(Collectors.toList())
                : Collections.emptyList();
    }
}
