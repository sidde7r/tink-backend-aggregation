package se.tink.backend.connector.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class PartnerAccountPayload {

    public static final String IGNORE_BALANCE = "IGNORE_BALANCE";
    public static final String CALCULATE_BALANCE = "CALCULATE_BALANCE";

    @JsonProperty(IGNORE_BALANCE)
    private boolean ignoreBalance;

    @JsonProperty(CALCULATE_BALANCE)
    private boolean calculateBalance;

    public boolean isIgnoreBalance() {
        return ignoreBalance;
    }

    public boolean isCalculateBalance() {
        return calculateBalance;
    }

    public void setIgnoreBalance(boolean ignoreBalance) {
        this.ignoreBalance = ignoreBalance;
    }

    public void setCalculateBalance(boolean calculateBalance) {
        this.calculateBalance = calculateBalance;
    }
}
