package se.tink.backend.aggregation.agents.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDate {

    private TransactionDateType type;
    private AvailableDateInformation value;

    public TransactionDateType getType() {
        return type;
    }

    public void setType(TransactionDateType type) {
        this.type = type;
    }

    public AvailableDateInformation getValue() {
        return value;
    }

    public void setValue(AvailableDateInformation value) {
        this.value = value;
    }
}
