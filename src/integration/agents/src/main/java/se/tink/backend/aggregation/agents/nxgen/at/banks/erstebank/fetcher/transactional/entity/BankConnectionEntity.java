package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankConnectionEntity {
    private String iban;
    private String countryCode;
    private String bic;

    public String getIban() {
        return iban;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getBic() {
        return bic;
    }
}
