package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @JsonProperty("TenYears")
    private double tenYears;

    public double getOneDay() {
        return oneDay;
    }

    public void setOneDay(double oneDay) {
        this.oneDay = oneDay;
    }

    public double getOneWeek() {
        return oneWeek;
    }

    public void setOneWeek(double oneWeek) {
        this.oneWeek = oneWeek;
    }

    public double getOneMonth() {
        return oneMonth;
    }

    public void setOneMonth(double oneMonth) {
        this.oneMonth = oneMonth;
    }

    public double getThreeMonths() {
        return threeMonths;
    }

    public void setThreeMonths(double threeMonths) {
        this.threeMonths = threeMonths;
    }

    public double getSixMonths() {
        return sixMonths;
    }

    public void setSixMonths(double sixMonths) {
        this.sixMonths = sixMonths;
    }

    public double getThisYear() {
        return thisYear;
    }

    public void setThisYear(double thisYear) {
        this.thisYear = thisYear;
    }

    public double getOneYear() {
        return oneYear;
    }

    public void setOneYear(double oneYear) {
        this.oneYear = oneYear;
    }

    public double getThreeYears() {
        return threeYears;
    }

    public void setThreeYears(double threeYears) {
        this.threeYears = threeYears;
    }

    public double getFiveYears() {
        return fiveYears;
    }

    public void setFiveYears(double fiveYears) {
        this.fiveYears = fiveYears;
    }

    public double getTenYears() {
        return tenYears;
    }

    public void setTenYears(double tenYears) {
        this.tenYears = tenYears;
    }
}
