package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItem {

    @JsonProperty("balanceType")
    private String balanceType;

    @JsonProperty("lastChangeDateTime")
    private String lastChangeDateTime;

    @JsonProperty("balanceAmount")
    private BalanceAmount balanceAmount;

    public void setBalanceType(final String balanceType) {
        this.balanceType = balanceType;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public void setLastChangeDateTime(final String lastChangeDateTime) {
        this.lastChangeDateTime = lastChangeDateTime;
    }

    public String getLastChangeDateTime() {
        return lastChangeDateTime;
    }

    public void setBalanceAmount(final BalanceAmount balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public BalanceAmount getBalanceAmount() {
        return balanceAmount;
    }
}
