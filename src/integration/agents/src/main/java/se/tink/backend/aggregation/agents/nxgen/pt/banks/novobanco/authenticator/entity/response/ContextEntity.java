package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ContextEntity {
    @JsonProperty("Contas")
    private AccountsEntity accounts;

    @JsonProperty("Selected")
    private String selected;

    public AccountsEntity getAccounts() {
        return accounts;
    }

    public String getSelected() {
        return selected;
    }

    public Optional<AccountDetailsEntity> getAccountDetails(String accountId) {
        return Optional.ofNullable(accounts)
                .map(AccountsEntity::getList)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(acc -> accountId.equals(acc.getId()))
                .findFirst();
    }
}
