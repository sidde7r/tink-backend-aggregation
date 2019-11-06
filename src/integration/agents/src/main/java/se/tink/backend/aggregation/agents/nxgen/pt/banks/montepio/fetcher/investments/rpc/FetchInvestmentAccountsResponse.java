package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.investments.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.fetcher.investments.entities.InvestmentAccountsResultEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchInvestmentAccountsResponse extends GenericResponse {

    @JsonProperty("Result")
    private InvestmentAccountsResultEntity result;

    public InvestmentAccountsResultEntity getResult() {
        return result;
    }
}
