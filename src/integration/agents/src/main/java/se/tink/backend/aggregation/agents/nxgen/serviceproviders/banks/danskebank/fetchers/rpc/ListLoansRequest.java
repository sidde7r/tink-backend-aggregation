package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ListLoansRequest {
    private String languageCode;

    private ListLoansRequest(String languageCode) {
        this.languageCode = languageCode;
    }

    public static ListLoansRequest createFromLanguageCode(String languageCode) {
        return new ListLoansRequest(languageCode);
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
