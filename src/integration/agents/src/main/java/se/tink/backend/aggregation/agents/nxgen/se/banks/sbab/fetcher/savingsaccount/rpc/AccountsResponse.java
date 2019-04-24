package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.savingsaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {
    @JsonProperty("accounts")
    private List<AccountEntity> accounts;

    public List<AccountEntity> getAccounts() {
        return Optional.ofNullable(accounts).orElseGet(Collections::emptyList);
    }
}
