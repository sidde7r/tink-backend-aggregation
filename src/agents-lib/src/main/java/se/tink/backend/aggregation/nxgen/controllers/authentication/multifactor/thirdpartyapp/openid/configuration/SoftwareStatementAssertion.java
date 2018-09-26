package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SoftwareStatementAssertion {

    private String assertion;
    private String softwareId;
    private String[] redirectUris;

    public String getAssertion() {
        return assertion;
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public String[] getRedirectUris() {
        return redirectUris;
    }
}
