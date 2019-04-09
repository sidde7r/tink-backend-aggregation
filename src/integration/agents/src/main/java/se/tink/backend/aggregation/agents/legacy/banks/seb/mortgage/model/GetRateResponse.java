package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.text.ParseException;
import java.util.Date;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class GetRateResponse {

    private Double indicativeRate = null;
    private Date dateValid = null;

    @JsonProperty("indicative_rate")
    public Double getIndicativeRate() {
        return indicativeRate;
    }

    public void setIndicativeRate(Double indicativeRate) {
        this.indicativeRate = indicativeRate;
    }

    @JsonProperty("date_valid")
    public Date getDateValid() {
        return dateValid;
    }

    public void setDateValid(String dateValid) {
        try {
            this.dateValid = ThreadSafeDateFormat.FORMATTER_SECONDS_T.parse(dateValid);
        } catch (ParseException e) {
            throw new IllegalArgumentException(String.format("Invalid date: %s", dateValid));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GetRateResponse that = (GetRateResponse) o;

        return Objects.equal(this.indicativeRate, that.indicativeRate)
                && Objects.equal(this.dateValid, that.dateValid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(indicativeRate, dateValid);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("indicativeRate", indicativeRate)
                .add("dateValid", dateValid)
                .toString();
    }
}
