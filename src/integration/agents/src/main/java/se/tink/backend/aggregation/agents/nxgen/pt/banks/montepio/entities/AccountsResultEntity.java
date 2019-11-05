package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResultEntity {

    @JsonProperty("CustomerProducts")
    private List<AccountEntity> accounts;

    public Optional<List<AccountEntity>> getAccounts() {
        return Optional.ofNullable(accounts);
    }
}
