package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountsResponseEntity {

    @JsonProperty("_links")
    private List<LinkEntity> links;

    private List<AccountEntity> accounts;

    public Collection<TransactionalAccount> toTinkAccounts() {
        return Optional.of(accounts).orElse(Collections.emptyList()).stream()
                .map(AccountEntity::toTinkAccount)
                .collect(Collectors.toList());
    }
}
