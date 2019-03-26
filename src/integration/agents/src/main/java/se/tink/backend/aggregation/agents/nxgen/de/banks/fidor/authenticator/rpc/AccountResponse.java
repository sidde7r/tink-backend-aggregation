package se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fidor.authenticator.entities.CollectionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class AccountResponse {
    @JsonProperty("data")
    private List<AccountEntity> accountEntity;

    private CollectionEntity collection;

    public List<AccountEntity> getAccountEntity() {
        return accountEntity;
    }

    public CollectionEntity getCollection() {
        return collection;
    }

    public Collection<TransactionalAccount> toTransactionalAccounts() {
        return accountEntity.stream()
                .map(AccountEntity::toTransactionalAccount)
                .collect(Collectors.toList());
    }
}
