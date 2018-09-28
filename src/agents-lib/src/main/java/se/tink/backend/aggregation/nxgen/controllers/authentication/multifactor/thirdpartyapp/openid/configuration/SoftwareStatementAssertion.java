package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SoftwareStatementAssertion {

    private String assertion;
    private String softwareId;
    private String redirectUri;

    public String getAssertion() {
        return assertion;
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
