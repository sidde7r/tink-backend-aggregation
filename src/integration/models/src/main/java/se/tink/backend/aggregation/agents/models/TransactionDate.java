package se.tink.backend.aggregation.agents.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDate {

    private TransactionDateType type;
    private LocalDateTime value;

    public TransactionDateType getType() {
        return type;
    }

    public void setType(TransactionDateType type) {
        this.type = type;
    }

    public LocalDateTime getValue() {
        return value;
    }

    public void setValue(LocalDateTime value) {
        this.value = value;
    }
}
