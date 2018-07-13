package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

public class ListCardsRequest {
    private String languageCode;

    private ListCardsRequest(String languageCode) {
        this.languageCode = languageCode;
    }

    public static ListCardsRequest create(String languageCode) {
        return new ListCardsRequest(languageCode);
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
