package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.fetchers.transactional.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;

@JsonObject
public class AccountWrapper {
    @JsonProperty("MonetaryAccountBank")
    private AccountEntity account;

    public AccountEntity getAccount() {
        return account;
    }

    public Optional<CheckingAccount> toTinkAccount() {
        return Optional.ofNullable(account)
                .map(AccountEntity::toTinkCheckingAccount)
                .filter(Optional::isPresent)
                .map(Optional::get);
    }
}
