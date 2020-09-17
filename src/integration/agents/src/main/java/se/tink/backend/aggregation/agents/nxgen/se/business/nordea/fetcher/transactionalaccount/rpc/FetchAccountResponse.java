package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountResponse {
    @JsonProperty("result")
    private List<AccountEntity> accounts;

    @JsonIgnore
    public List<TransactionalAccount> toTinkAccount() {
        return getAccounts().stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(AccountEntity::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<AccountEntity> getAccounts() {
        return accounts;
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
