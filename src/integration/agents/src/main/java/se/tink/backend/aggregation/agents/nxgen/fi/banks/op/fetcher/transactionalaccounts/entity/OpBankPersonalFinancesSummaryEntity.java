package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.transactionalaccounts.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankPersonalFinancesSummaryEntity {
    private boolean summaryAvailable;
    private String openToBuy;
    private String confirmed;
    private String unconfirmed;
    private String dailySpending;
    private String dailySpendingCard;
    private int dailySpendingDaysLeft;
    private int daysUntilNextIncome;
    private boolean accountEligibleForPersonalFinance;

    public boolean isSummaryAvailable() {
        return summaryAvailable;
    }

    public String getOpenToBuy() {
        return openToBuy;
    }

    public String getConfirmed() {
        return confirmed;
    }

    public String getUnconfirmed() {
        return unconfirmed;
    }

    public String getDailySpending() {
        return dailySpending;
    }

    public String getDailySpendingCard() {
        return dailySpendingCard;
    }

    public int getDailySpendingDaysLeft() {
        return dailySpendingDaysLeft;
    }

    public int getDaysUntilNextIncome() {
        return daysUntilNextIncome;
    }

    public boolean isAccountEligibleForPersonalFinance() {
        return accountEligibleForPersonalFinance;
    }
}
