package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities.SavingAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class SavingAccountResponse {
    private Body body;

    @JsonObject
    @Getter
    public static class Body {
        @JsonProperty("listaLibretti")
        private List<SavingAccountEntity> savingAccountList;
    }

    public Optional<List<SavingAccountEntity>> getSavingAccounts() {
        return Optional.ofNullable(getBody().getSavingAccountList());
    }
}
