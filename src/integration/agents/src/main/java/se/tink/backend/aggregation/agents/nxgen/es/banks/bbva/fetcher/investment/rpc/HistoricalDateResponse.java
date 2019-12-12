package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities.HistoricalDateEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HistoricalDateResponse {
    // private List<InvestmentsSummaryEntity> investments;
    private HistoricalDateEntity historicalDate;

    @JsonIgnore
    public Date getMaxHistoricalDate() {
        return historicalDate.getMaxHistoricalDate();
    }
}
