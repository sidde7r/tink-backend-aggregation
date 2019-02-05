package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DevelopmentEntity {
    @JsonProperty("OneDay")
    private double oneDay;
    @JsonProperty("OneWeek")
    private double oneWeek;
    @JsonProperty("OneMonth")
    private double oneMonth;
    @JsonProperty("ThreeMonths")
    private double threeMonths;
    @JsonProperty("SixMonths")
    private double sixMonths;
    @JsonProperty("ThisYear")
    private double thisYear;
    @JsonProperty("OneYear")
    private double oneYear;
    @JsonProperty("ThreeYears")
    private double threeYears;
    @JsonProperty("FiveYears")
    private double fiveYears;

    public double getOneDay() {
        return oneDay;
    }

    public double getOneWeek() {
        return oneWeek;
    }

    public double getOneMonth() {
        return oneMonth;
    }

    public double getThreeMonths() {
        return threeMonths;
    }

    public double getSixMonths() {
        return sixMonths;
    }

    public double getThisYear() {
        return thisYear;
    }

    public double getOneYear() {
        return oneYear;
    }

    public double getThreeYears() {
        return threeYears;
    }

    public double getFiveYears() {
        return fiveYears;
    }
}
