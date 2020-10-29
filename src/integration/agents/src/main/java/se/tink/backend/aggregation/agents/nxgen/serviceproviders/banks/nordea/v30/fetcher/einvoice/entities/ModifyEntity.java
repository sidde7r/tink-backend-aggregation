package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.einvoice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ModifyEntity {
    private boolean amount;
    private boolean due;
    private boolean from;
    private boolean message;

    @JsonProperty("recurring_count")
    private boolean recurringCount;

    @JsonProperty("recurring_interval")
    private boolean recurringInterval;

    @JsonProperty("recurring_lastday")
    private boolean recurringLastday;

    @JsonProperty("recurring_repeats")
    private boolean recurringRepeats;

    private boolean to;
    private boolean type;

    public boolean isAmount() {
        return amount;
    }

    public boolean isDue() {
        return due;
    }

    public boolean isFrom() {
        return from;
    }

    public boolean isMessage() {
        return message;
    }

    public boolean isRecurringCount() {
        return recurringCount;
    }

    public boolean isRecurringInterval() {
        return recurringInterval;
    }

    public boolean isRecurringLastday() {
        return recurringLastday;
    }

    public boolean isRecurringRepeats() {
        return recurringRepeats;
    }

    public boolean isTo() {
        return to;
    }

    public boolean isType() {
        return type;
    }
}
