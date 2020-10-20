package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.entity.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AccountsResponse {

    private Body body;

    @JsonObject
    @Getter
    public static class Body {
        @JsonProperty("listaConti")
        public List<AccountEntity> accounts;
    }

    public Optional<List<AccountEntity>> getAccounts() {
        return Optional.ofNullable(getBody().getAccounts());
    }
}
