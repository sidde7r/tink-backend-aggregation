package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class GetCountryInput {
    private String applCd;
    private String language;

    public String getApplCd() {
        return applCd;
    }

    public void setApplCd(String applCd) {
        this.applCd = applCd;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
