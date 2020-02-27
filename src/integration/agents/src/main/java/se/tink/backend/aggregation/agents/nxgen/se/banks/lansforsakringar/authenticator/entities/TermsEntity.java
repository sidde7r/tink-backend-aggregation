package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TermsEntity {
    private String title;
    private String url;
    private String acceptanceHeader;

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getAcceptanceHeader() {
        return acceptanceHeader;
    }
}
