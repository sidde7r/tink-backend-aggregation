package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

public class CardsListRequest {
    private String languageCode;

    private CardsListRequest(String languageCode) {
        this.languageCode = languageCode;
    }

    public static CardsListRequest create(String languageCode) {
        return new CardsListRequest(languageCode);
    }

    public String getLanguageCode() {
        return languageCode;
    }
}
