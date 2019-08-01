package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TaxPeriodEntity {
    @JsonProperty("year")
    private String year = null;

    @JsonProperty("type")
    private TaxRecordPeriodCodeEntity type = null;

    @JsonProperty("fromDate")
    private LocalDate fromDate = null;

    @JsonProperty("toDate")
    private LocalDate toDate = null;

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public TaxRecordPeriodCodeEntity getType() {
        return type;
    }

    public void setType(TaxRecordPeriodCodeEntity type) {
        this.type = type;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate = fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate = toDate;
    }
}
