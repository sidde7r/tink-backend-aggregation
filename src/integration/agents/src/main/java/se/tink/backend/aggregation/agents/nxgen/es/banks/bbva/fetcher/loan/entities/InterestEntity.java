package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestEntity {
    private double differentialPercentage;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date reviewDate;

    private double percentage;

    public double getDifferentialPercentage() {
        return differentialPercentage;
    }

    public Date getReviewDate() {
        return reviewDate;
    }

    public double getPercentage() {
        return percentage;
    }
}
