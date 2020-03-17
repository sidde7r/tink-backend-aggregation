package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities.ResponseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@JsonObject
public class FetchPensionWithLifeInsuranceResponse {
    // `id` is null - cannot define it!
    private ResponseEntity response;
    // `errors` is null - cannot define it!

    public ResponseEntity getResponse() {
        return response;
    }

    @JsonIgnore
    public InvestmentAccount getTinkInvestmentAccounts() {
        return null;
    }
}
