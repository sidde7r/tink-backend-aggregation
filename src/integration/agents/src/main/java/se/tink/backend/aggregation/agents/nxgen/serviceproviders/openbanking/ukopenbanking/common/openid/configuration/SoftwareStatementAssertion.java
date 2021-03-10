package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@JsonObject
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SoftwareStatementAssertion {

    private final String softwareId;

    private final URL jwksEndpoint;

    private final String orgId;

    @SneakyThrows
    public static SoftwareStatementAssertion fromJWT(String jwt) {
        JWTClaimsSet claimsSet = SignedJWT.parse(jwt).getJWTClaimsSet();
        return new SoftwareStatementAssertion(
                claimsSet.getStringClaim(OpenIdConstants.Params.SOFTWARE_ID),
                new URL(claimsSet.getStringClaim(OpenIdConstants.Params.SOFTWARE_JWKS_ENDPOINT)),
                claimsSet.getStringClaim(OpenIdConstants.Params.ORG_ID));
    }

    @SneakyThrows
    public static SoftwareStatementAssertion fromJson(String json) {
        JsonNode jsonNode = new ObjectMapper().readTree(json);
        String softwareId = jsonNode.get(OpenIdConstants.Params.SOFTWARE_ID).asText();
        String url = jsonNode.get(OpenIdConstants.Params.SOFTWARE_JWKS_ENDPOINT).asText();
        String orgId =
                Optional.ofNullable(jsonNode.get(OpenIdConstants.Params.ORG_ID))
                        .map(JsonNode::asText)
                        .orElse(null);
        return new SoftwareStatementAssertion(softwareId, new URL(url), orgId);
    }
}
