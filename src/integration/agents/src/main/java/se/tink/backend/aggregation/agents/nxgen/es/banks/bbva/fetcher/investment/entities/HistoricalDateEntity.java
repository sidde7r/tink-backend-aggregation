package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HistoricalDateEntity {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date maxHistoricalDate;

    private boolean isStartContracts;

    public Date getMaxHistoricalDate() {
        return maxHistoricalDate;
    }
}
