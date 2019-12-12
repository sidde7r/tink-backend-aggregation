package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BasicEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentsSummaryEntity {
    private String id;
    private List<NumberFormatsEntity> numberFormats;
    private List<BasicEntity> stockMarkets;
    private List<SummariesEntity> summaries;

    public List<SummariesEntity> getSummaries() {
        return summaries;
    }
}
