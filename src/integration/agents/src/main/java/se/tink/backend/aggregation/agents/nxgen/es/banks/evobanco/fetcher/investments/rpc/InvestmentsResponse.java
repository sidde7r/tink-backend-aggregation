package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.investments.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.List;

@JsonObject
public class InvestmentsResponse {
    private List<Object> investments;

    public List<Object> getInvestments() {
        return investments;
    }
}
