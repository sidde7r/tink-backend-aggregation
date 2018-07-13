package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDetailsSummaryEntity {
    private ParameterEntity statementBalance;
    private ParameterEntity recentPayments;
    private ParameterEntity recentCharges;
    private ParameterEntity totalBalance;
    private ParameterEntity paymentDue;
    private ParameterEntity commonPaymentDue;
    private ParameterEntity paymentDueDate;
    private ParameterEntity paymentDueOnDate;
    private ParameterEntity lastStmtBalanceClosingDate;
    private ParameterEntity latestTransactionStartDate;
    private ParameterEntity timeLineBalance;
    private ParameterEntity timelinePaymentBalance;

    public ParameterEntity getStatementBalance() {
        return statementBalance;
    }

    public void setStatementBalance(ParameterEntity statementBalance) {
        this.statementBalance = statementBalance;
    }

    public ParameterEntity getRecentPayments() {
        return recentPayments;
    }

    public void setRecentPayments(ParameterEntity recentPayments) {
        this.recentPayments = recentPayments;
    }

    public ParameterEntity getRecentCharges() {
        return recentCharges;
    }

    public void setRecentCharges(ParameterEntity recentCharges) {
        this.recentCharges = recentCharges;
    }

    public ParameterEntity getTotalBalance() {
        return totalBalance;
    }

    public void setTotalBalance(ParameterEntity totalBalance) {
        this.totalBalance = totalBalance;
    }

    public ParameterEntity getPaymentDue() {
        return paymentDue;
    }

    public void setPaymentDue(ParameterEntity paymentDue) {
        this.paymentDue = paymentDue;
    }

    public ParameterEntity getCommonPaymentDue() {
        return commonPaymentDue;
    }

    public void setCommonPaymentDue(ParameterEntity commonPaymentDue) {
        this.commonPaymentDue = commonPaymentDue;
    }

    public ParameterEntity getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(ParameterEntity paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }

    public ParameterEntity getPaymentDueOnDate() {
        return paymentDueOnDate;
    }

    public void setPaymentDueOnDate(ParameterEntity paymentDueOnDate) {
        this.paymentDueOnDate = paymentDueOnDate;
    }

    public ParameterEntity getLastStmtBalanceClosingDate() {
        return lastStmtBalanceClosingDate;
    }

    public void setLastStmtBalanceClosingDate(ParameterEntity lastStmtBalanceClosingDate) {
        this.lastStmtBalanceClosingDate = lastStmtBalanceClosingDate;
    }

    public ParameterEntity getLatestTransactionStartDate() {
        return latestTransactionStartDate;
    }

    public void setLatestTransactionStartDate(ParameterEntity latestTransactionStartDate) {
        this.latestTransactionStartDate = latestTransactionStartDate;
    }

    public ParameterEntity getTimeLineBalance() {
        return timeLineBalance;
    }

    public void setTimeLineBalance(ParameterEntity timeLineBalance) {
        this.timeLineBalance = timeLineBalance;
    }

    public ParameterEntity getTimelinePaymentBalance() {
        return timelinePaymentBalance;
    }

    public void setTimelinePaymentBalance(ParameterEntity timelinePaymentBalance) {
        this.timelinePaymentBalance = timelinePaymentBalance;
    }
}
