package se.tink.backend.connector.rpc.seb;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PartnerAccountPayload {

    public static final String IGNORE_BALANCE = "IGNORE_BALANCE";

    @JsonProperty(IGNORE_BALANCE)
    private boolean ignoreBalance;

    public boolean isIgnoreBalance() {
        return ignoreBalance;
    }

    public void setIgnoreBalance(boolean ignoreBalance) {
        this.ignoreBalance = ignoreBalance;
    }
}
