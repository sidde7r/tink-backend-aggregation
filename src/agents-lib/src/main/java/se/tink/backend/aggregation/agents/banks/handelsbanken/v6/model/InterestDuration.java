package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterestDuration {

    private String durationDay;
    private String interest;
    private String year;

    public String getDurationDay() {
        return durationDay;
    }

    public String getInterest() {
        return interest;
    }

    public String getYear() {
        return year;
    }

    public void setDurationDay(String durationDay) {
        this.durationDay = durationDay;
    }

    public void setInterest(String interest) {
        this.interest = interest;
    }

    public void setYear(String year) {
        this.year = year;
    }

}
