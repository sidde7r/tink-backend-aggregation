package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.auth0.jwt.JWT;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants;

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

    public String[] getAllRedirectUris() {
        return JWT.decode(assertion)
                .getClaim(OpenIdConstants.Params.SOFTWARE_REDIRECT_URIS)
                .asArray(String.class);
    }
}
