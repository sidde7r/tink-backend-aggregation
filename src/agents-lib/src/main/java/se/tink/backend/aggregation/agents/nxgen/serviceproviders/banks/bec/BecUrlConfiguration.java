package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

public final class BecUrlConfiguration {

    private String payload;

    public BecUrlConfiguration(String payload) {
        this.payload = payload;
    }

    String getBaseUrl() {
        return "https://eticket" + payload + ".prod.bec.dk";
    }

    String getMobilbankUrl() {
        return getBaseUrl() + "/mobilbank";
    }

    public String getAppSync() {
        return getMobilbankUrl() + BecConstants.Url.APP_SYNC;
    }

    public String getLoginChallenge() {
        return getMobilbankUrl() + BecConstants.Url.LOGIN_CHALLENGE;
    }

    public String getFetchAccounts() {
        return getMobilbankUrl() + BecConstants.Url.FETCH_ACCOUNTS;
    }

    public String getFetchAccountDetails() {
        return getMobilbankUrl() + BecConstants.Url.FETCH_ACCOUNT_DETAIL;
    }

    public String getFetchAccountTransactions() {
        return getMobilbankUrl() + BecConstants.Url.FETCH_ACCOUNT_TRANSACTIONS;
    }

    public String getFetchAccountUpcomingTransactions() {
        return getMobilbankUrl() + BecConstants.Url.FETCH_ACCOUNT_UPCOMING_TRANSACTIONS;
    }

    public String getFetchCard() {
        return getMobilbankUrl() + BecConstants.Url.FETCH_CARD;
    }

    public String getFetchLoan() {
        return getMobilbankUrl() + BecConstants.Url.FETCH_LOAN;
    }

    public String getFetchLoanDetails() {
        return getMobilbankUrl() + BecConstants.Url.FETCH_LOAN_DETAILS;
    }

    public String getFetchDepot() {
        return getMobilbankUrl() + BecConstants.Url.FETCH_DEPOT;
    }

    public String getLogout() {
        return getMobilbankUrl() + BecConstants.Url.LOGOUT;
    }
}
