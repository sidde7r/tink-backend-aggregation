package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FinancialTab {

    private StatementBalance statementBalance;
    private PaymentDueInfo paymentDueInfo;
    private AvailableCredit availableCredit;
    private BalanceDue balanceDue;
    private TotalBalance totalBalance;
    private TransactionAndStatementsCTA transactionAndStatementsCTA;
    private RecentCharges recentCharges;
    private RecentPaymentsAndCredits recentPaymentsAndCredits;

    public StatementBalance getStatementBalance() {
        return statementBalance;
    }

    public PaymentDueInfo getPaymentDueInfo() {
        return paymentDueInfo;
    }

    public AvailableCredit getAvailableCredit() {
        return availableCredit;
    }

    public BalanceDue getBalanceDue() {
        return balanceDue;
    }

    public TotalBalance getTotalBalance() {
        return totalBalance;
    }

    public TransactionAndStatementsCTA getTransactionAndStatementsCTA() {
        return transactionAndStatementsCTA;
    }

    public RecentCharges getRecentCharges() {
        return recentCharges;
    }

    public RecentPaymentsAndCredits getRecentPaymentsAndCredits() {
        return recentPaymentsAndCredits;
    }
}
