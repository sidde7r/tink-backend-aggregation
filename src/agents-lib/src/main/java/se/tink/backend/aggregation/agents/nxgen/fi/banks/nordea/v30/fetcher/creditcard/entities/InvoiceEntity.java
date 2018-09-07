package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvoiceEntity {
    @JsonProperty("next_sum")
    private double nextSum;
    @JsonProperty("next_date")
    private String nextDate;
    @JsonProperty("prev_total")
    private double prevTotal;
    @JsonProperty("prev_date")
    private String prevDate;
    @JsonProperty("due_day_of_month")
    private int dueDayOfMonth;
    @JsonProperty("reference_number")
    private String referenceNumber;

    public String getReferenceNumber() {
        return referenceNumber;
    }
}
