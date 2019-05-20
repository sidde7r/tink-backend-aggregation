package se.tink.backend.aggregation.agents.nxgen.fi.banks.danskebank.rpc;

public class FetchHouseholdFIRequest {
    private String languageCode;

    private FetchHouseholdFIRequest(String languageCode) {
        this.languageCode = languageCode;
    }

    public static FetchHouseholdFIRequest createFromLanguageCode(String languageCode) {
        return new FetchHouseholdFIRequest(languageCode);
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
