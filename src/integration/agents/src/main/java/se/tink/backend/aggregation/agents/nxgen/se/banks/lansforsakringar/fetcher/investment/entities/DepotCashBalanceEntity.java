package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DepotCashBalanceEntity {
    private String moneyAvailableForPurchase;
    private String moneyAvailableForWithdrawal;
    private String balance;

    public String getMoneyAvailableForPurchase() {
        return moneyAvailableForPurchase;
    }

    public String getMoneyAvailableForWithdrawal() {
        return moneyAvailableForWithdrawal;
    }

    public String getBalance() {
        return balance;
    }
}
