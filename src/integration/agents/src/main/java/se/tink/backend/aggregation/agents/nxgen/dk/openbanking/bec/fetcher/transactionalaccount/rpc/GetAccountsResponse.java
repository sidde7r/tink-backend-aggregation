package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.rpc;

import se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

import java.util.List;
import java.util.stream.Collectors;

@JsonObject
public class GetAccountsResponse {

    private List<AccountEntity> accounts;

    public List<TransactionalAccount> toTinkAccounts() {
        return accounts.stream().map(AccountEntity::toTinkAccount).collect(Collectors.toList());
    }
}
