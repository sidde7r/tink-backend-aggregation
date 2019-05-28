package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Balance {

    private BalanceAmount balanceAmount;
    private String balanceType;

    public BalanceAmount getBalanceAmount() {
        return balanceAmount;
    }

    public void setBalanceAmount(final BalanceAmount balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public void setBalanceType(final String balanceType) {
        this.balanceType = balanceType;
    }
}
