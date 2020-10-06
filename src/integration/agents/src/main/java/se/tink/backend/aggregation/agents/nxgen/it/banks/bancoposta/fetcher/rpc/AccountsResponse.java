package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsResponse {

    @JsonProperty("data")
    private List<AccountEntity> accounts;

    public Optional<List<AccountEntity>> getAccounts() {
        return Optional.ofNullable(accounts);
    }
}
