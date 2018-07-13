package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.transactionalaccount.entitites.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class AccountListResponse {
    @JsonProperty("list")
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccountList() {
        return accounts;
    }

    public List<TransactionalAccount> toTinkAccounts() {
        return accounts.stream()
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}

