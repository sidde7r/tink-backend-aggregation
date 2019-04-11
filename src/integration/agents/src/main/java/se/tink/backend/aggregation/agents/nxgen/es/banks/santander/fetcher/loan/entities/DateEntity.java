package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DateEntity {

    @JsonProperty("dia")
    private String day;

    @JsonProperty("mes")
    private String month;

    @JsonProperty("anyo")
    private String year;

    public void setDay(String day) {
        this.day = day;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
