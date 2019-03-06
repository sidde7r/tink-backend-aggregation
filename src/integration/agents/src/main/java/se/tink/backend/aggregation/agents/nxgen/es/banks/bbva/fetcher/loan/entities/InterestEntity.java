package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InterestEntity {
    private double differentialPercentage;
    private String reviewDate;
    private double percentage;

    public double getDifferentialPercentage() {
        return differentialPercentage;
    }

    public String getReviewDate() {
        return reviewDate;
    }

    public double getPercentage() {
        return percentage;
    }
}
