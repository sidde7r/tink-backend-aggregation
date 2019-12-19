package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BasicEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentsSummaryEntity {
    private String id;
    private List<NumberFormatsEntity> numberFormats;
    private List<BasicEntity> stockMarkets;
    private List<SummariesEntity> summaries;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date openingDate;

    public List<SummariesEntity> getSummaries() {
        return summaries;
    }

    public Date getOpeningDate() {
        return openingDate;
    }
}
