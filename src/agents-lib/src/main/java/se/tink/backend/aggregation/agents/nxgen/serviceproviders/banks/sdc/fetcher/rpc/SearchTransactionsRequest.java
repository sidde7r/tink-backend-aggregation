package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

public class SearchTransactionsRequest {
    private String agreementId;
    private String accountId;
    private String transactionsTo;
    private boolean includeReservations;
    private String transactionsFrom;

    public String getAgreementId() {
        return agreementId;
    }

    public SearchTransactionsRequest setAgreementId(String agreementId) {
        this.agreementId = agreementId;
        return this;
    }

    public String getAccountId() {
        return accountId;
    }

    public SearchTransactionsRequest setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public String getTransactionsTo() {
        return transactionsTo;
    }

    public SearchTransactionsRequest setTransactionsTo(String transactionsTo) {
        this.transactionsTo = transactionsTo;
        return this;
    }

    public boolean isIncludeReservations() {
        return includeReservations;
    }

    public SearchTransactionsRequest setIncludeReservations(boolean includeReservations) {
        this.includeReservations = includeReservations;
        return this;
    }

    public String getTransactionsFrom() {
        return transactionsFrom;
    }

    public SearchTransactionsRequest setTransactionsFrom(String transactionsFrom) {
        this.transactionsFrom = transactionsFrom;
        return this;
    }
}
