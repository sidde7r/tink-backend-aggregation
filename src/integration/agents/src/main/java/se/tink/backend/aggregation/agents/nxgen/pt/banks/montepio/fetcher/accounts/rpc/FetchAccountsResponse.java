package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.AccountsResultEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountsResponse extends GenericResponse {

    @JsonProperty("Result")
    private AccountsResultEntity resultEntity;

    public AccountsResultEntity getResultEntity() {
        return resultEntity;
    }
}
