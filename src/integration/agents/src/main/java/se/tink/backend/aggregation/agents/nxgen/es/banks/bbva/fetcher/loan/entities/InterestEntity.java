package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults.TIMEZONE_CET;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestEntity {
    private Double differentialPercentage;
    private Date reviewDate;

    private Double percentage;

    public Double getDifferentialPercentage() {
        return differentialPercentage;
    }

    public LocalDate getReviewDateAsLocalDate() {
        return reviewDate.toInstant().atZone(ZoneId.of(TIMEZONE_CET)).toLocalDate();
    }

    public Double getPercentage() {
        return percentage;
    }
}
