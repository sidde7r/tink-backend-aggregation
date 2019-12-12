package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FilterRangesEntity {
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date startDateRange;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date endDateRange;

    private boolean isGroupByDate;
    private boolean isProfitabilityFilter;

    public FilterRangesEntity() {}

    public FilterRangesEntity(Date startDate) {
        this.startDateRange = startDate;
        Calendar calendar = Calendar.getInstance();
        this.endDateRange = calendar.getTime();
        this.isGroupByDate = false;
        this.isProfitabilityFilter = true;
    }

    public Date getStartDateRange() {
        return startDateRange;
    }
}
