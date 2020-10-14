package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.Params;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
public class SoftwareStatementAssertion {

    private final String softwareId;

    private final URL jwksEndpoint;

    private SoftwareStatementAssertion(String softwareId, URL jwksEndpoint) {
        this.softwareId = softwareId;
        this.jwksEndpoint = jwksEndpoint;
    }

    @lombok.SneakyThrows
    public static SoftwareStatementAssertion fromJWT(String jwt) {
        JWTClaimsSet claimsSet = SignedJWT.parse(jwt).getJWTClaimsSet();
        return new SoftwareStatementAssertion(
                claimsSet.getStringClaim(Params.SOFTWARE_ID),
                new URL(claimsSet.getStringClaim(Params.SOFTWARE_JWKS_ENDPOINT)));
    }

    @lombok.SneakyThrows
    public static SoftwareStatementAssertion fromJson(String json) {
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        String softwareId = jsonNode.get(Params.SOFTWARE_ID).asText();
        return new SoftwareStatementAssertion(softwareId, new URL(""));
    }

    public String getSoftwareId() {
        return softwareId;
    }

    public URL getJwksEndpoint() {
        return jwksEndpoint;
    }
}
