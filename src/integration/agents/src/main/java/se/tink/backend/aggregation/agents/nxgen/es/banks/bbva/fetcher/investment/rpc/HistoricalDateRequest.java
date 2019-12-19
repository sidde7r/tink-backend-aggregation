package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.FilterInvestmentsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HistoricalDateRequest {
    private List<FilterInvestmentsEntity> investments;

    public HistoricalDateRequest(String portfolio) {
        this.investments = Collections.singletonList(new FilterInvestmentsEntity(portfolio));
    }
}
