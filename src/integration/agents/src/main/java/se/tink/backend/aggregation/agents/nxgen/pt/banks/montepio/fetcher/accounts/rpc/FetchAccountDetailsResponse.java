package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.accounts.entities.AccountDetailsResultEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchAccountDetailsResponse extends GenericResponse {

    @JsonProperty("Result")
    private AccountDetailsResultEntity result;

    public AccountDetailsResultEntity getResult() {
        return result;
    }
}
