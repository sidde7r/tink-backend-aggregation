package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.investments.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentsResponse {
    private List<Object> investments;

    public List<Object> getInvestments() {
        return investments;
    }
}
