package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@Getter
public class SoftwareStatementAssertion {

    private final String softwareId;

    private final URL jwksEndpoint;

    private SoftwareStatementAssertion(String softwareId, URL jwksEndpoint) {
        this.softwareId = softwareId;
        this.jwksEndpoint = jwksEndpoint;
    }

    @SneakyThrows
    public static SoftwareStatementAssertion fromJWT(String jwt) {
        JWTClaimsSet claimsSet = SignedJWT.parse(jwt).getJWTClaimsSet();
        return new SoftwareStatementAssertion(
                claimsSet.getStringClaim(OpenIdConstants.Params.SOFTWARE_ID),
                new URL(claimsSet.getStringClaim(OpenIdConstants.Params.SOFTWARE_JWKS_ENDPOINT)));
    }

    @SneakyThrows
    public static SoftwareStatementAssertion fromJson(String json) {
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        String softwareId = jsonNode.get(OpenIdConstants.Params.SOFTWARE_ID).asText();
        return new SoftwareStatementAssertion(softwareId, new URL(""));
    }
}
