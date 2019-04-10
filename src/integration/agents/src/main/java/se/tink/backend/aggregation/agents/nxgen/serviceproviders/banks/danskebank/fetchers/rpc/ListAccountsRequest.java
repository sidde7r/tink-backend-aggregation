package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

public class ListAccountsRequest {
    private String languageCode;

    private ListAccountsRequest(String languageCode) {
        this.languageCode = languageCode;
    }

    public static ListAccountsRequest createFromLanguageCode(String languageCode) {
        return new ListAccountsRequest(languageCode);
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
