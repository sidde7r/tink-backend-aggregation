package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.fetcher.transactionalaccount.entities.AccountOwnerEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

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

    @JsonIgnore
    public IdentityData getIdentityData(final Credentials credentials) {
        return accounts.stream()
                .map(AccountEntity::getRoles)
                .filter(Objects::nonNull)
                .filter(roleList -> roleList.size() == 1)
                .flatMap(List::stream)
                .filter(AccountOwnerEntity::isOwner)
                .map(accountEntity -> accountEntity.toIdentity(credentials))
                .distinct()
                .reduce(IdentityData::throwingMerger)
                .orElse(SeIdentityData.of("", credentials.getField(Key.USERNAME)));
    }

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setAccounts(List<AccountEntity> accounts) {
        this.accounts = accounts;
    }
}
