package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReturnsEntity {
    private Double thisYear;
    private Double thisYearTotal;
    private Boolean thisYearAllExists;
    private Double sinceBought;
    private Boolean sinceBoughtAllExists;

    public Double getThisYear() {
        return thisYear;
    }

    public Double getThisYearTotal() {
        return thisYearTotal;
    }

    public Boolean getThisYearAllExists() {
        return thisYearAllExists;
    }

    public Double getSinceBought() {
        return sinceBought;
    }

    public Boolean getSinceBoughtAllExists() {
        return sinceBoughtAllExists;
    }
}
