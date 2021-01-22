package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountsResponse {

    private List<AccountEntity> accounts;

    @JsonIgnore
    public Collection<AccountEntity> getTransactionalAccounts(String currency) {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(AccountEntity::isTransactionalAccount)
                .filter(account -> currency.equalsIgnoreCase(account.getCurrency()))
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public Collection<AccountEntity> getCreditCardAccounts(String currency) {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList()).stream()
                .filter(AccountEntity::isCardAccount)
                .filter(account -> currency.equalsIgnoreCase(account.getCurrency()))
                .collect(Collectors.toList());
    }
}
