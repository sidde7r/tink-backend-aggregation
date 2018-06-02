package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

public class FutureTransactionsRequest {
    private String languageCode;
    private String accountNoInt;

    private FutureTransactionsRequest(String languageCode, String accountNoInt) {
        this.languageCode = languageCode;
        this.accountNoInt = accountNoInt;
    }

    public static FutureTransactionsRequest create(String languageCode, String accountNoInt) {
        return new FutureTransactionsRequest(languageCode, accountNoInt);
    }

    public String getLanguageCode() {
        return languageCode;
    }

    public String getAccountNoInt() {
        return accountNoInt;
    }
}
