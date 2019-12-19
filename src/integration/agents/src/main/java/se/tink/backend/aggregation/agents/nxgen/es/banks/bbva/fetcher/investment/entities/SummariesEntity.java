package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.TypeEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SummariesEntity implements Comparable<SummariesEntity> {
    private List<FilterRangesEntity> filterRanges;
    private ReturnsEntity returns;
    private TypeEntity summaryType;

    @JsonIgnore
    public Date getStartDateRange() {
        return Optional.ofNullable(filterRanges).orElse(Collections.emptyList()).stream()
                .findFirst()
                .map(FilterRangesEntity::getStartDateRange)
                .orElse(null);
    }

    @JsonIgnore
    public BigDecimal getProfitAmount() {
        return returns.getProfitAmount();
    }

    @JsonIgnore
    @Override
    public int compareTo(SummariesEntity other) {
        Date date1 = this.getStartDateRange();
        Date date2 = other.getStartDateRange();
        if (date1 == null && date2 == null) {
            return 0;
        } else if (date1 == null) {
            return -1;
        } else if (date2 == null) {
            return 1;
        }
        return date1.compareTo(date2);
    }
}
