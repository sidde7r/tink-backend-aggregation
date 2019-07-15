package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class GetAccountsResponse {

    @JsonProperty("accounts")
    private List<AccountEntity> accountList;

    public List<AccountEntity> getAccountList() {
        return accountList;
    }

    public void setAccountList(List<AccountEntity> accountList) {
        this.accountList = accountList;
    }

    @JsonIgnore
    public List<TransactionalAccount> toTinkAcounts() {
        return Optional.ofNullable(accountList).orElse(Collections.emptyList()).stream()
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
