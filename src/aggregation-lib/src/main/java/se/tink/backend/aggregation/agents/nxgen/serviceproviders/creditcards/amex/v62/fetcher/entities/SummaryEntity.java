package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SummaryEntity {
    private SummaryFieldEntity statementBalance;
    private SummaryFieldEntity recentPayments;
    private SummaryFieldEntity recentCharges;
    private SummaryFieldEntity totalBalance;
    private SummaryFieldEntity paymentDue;
    private SummaryFieldEntity commonPaymentDue;
    private SummaryFieldEntity paymentDueDate;
    private SummaryFieldEntity paymentDueOnDate;
    private SummaryFieldEntity lastStmtBalanceClosingDate;
    private SummaryFieldEntity latestTransactionStartDate;
    private SummaryFieldEntity timeLineBalance;
    private SummaryFieldEntity timelinePaymentBalance;

    public SummaryFieldEntity getStatementBalance() {
        return statementBalance;
    }

    public SummaryFieldEntity getRecentPayments() {
        return recentPayments;
    }

    public SummaryFieldEntity getRecentCharges() {
        return recentCharges;
    }

    public SummaryFieldEntity getTotalBalance() {
        return totalBalance;
    }

    public SummaryFieldEntity getPaymentDue() {
        return paymentDue;
    }

    public SummaryFieldEntity getCommonPaymentDue() {
        return commonPaymentDue;
    }

    public SummaryFieldEntity getPaymentDueDate() {
        return paymentDueDate;
    }

    public SummaryFieldEntity getPaymentDueOnDate() {
        return paymentDueOnDate;
    }

    public SummaryFieldEntity getLastStmtBalanceClosingDate() {
        return lastStmtBalanceClosingDate;
    }

    public SummaryFieldEntity getLatestTransactionStartDate() {
        return latestTransactionStartDate;
    }

    public SummaryFieldEntity getTimeLineBalance() {
        return timeLineBalance;
    }

    public SummaryFieldEntity getTimelinePaymentBalance() {
        return timelinePaymentBalance;
    }
}
