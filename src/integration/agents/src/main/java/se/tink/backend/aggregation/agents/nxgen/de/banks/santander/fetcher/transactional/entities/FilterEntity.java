package se.tink.backend.aggregation.agents.nxgen.de.banks.santander.fetcher.transactional.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FilterEntity {

    @JsonProperty("iniDateRange")
    private String iniDateRange;

    @JsonProperty("endDateRange")
    private String endDateRange;

    public FilterEntity(Date from, Date to) {
        this.iniDateRange = from.toInstant().toString();
        this.endDateRange = to.toInstant().toString();
    }
}
