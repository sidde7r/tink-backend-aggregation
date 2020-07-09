package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults.TIMEZONE_CET;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestEntity {

    private BigDecimal differentialPercentage;
    private Date reviewDate;
    private BigDecimal percentage;

    public BigDecimal getDifferentialPercentage() {
        return differentialPercentage;
    }

    public LocalDate getReviewDateAsLocalDate() {
        return Optional.ofNullable(reviewDate)
                .map(Date::toInstant)
                .map(instant -> instant.atZone(ZoneId.of(TIMEZONE_CET)).toLocalDate())
                .orElse(null);
    }

    public BigDecimal getPercentage() {
        return percentage;
    }
}
