package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Base64;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Params;
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
                URL.of(claimsSet.getStringClaim(OpenIdConstants.Params.SOFTWARE_JWKS_ENDPOINT)),
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
        return new SoftwareStatementAssertion(softwareId, URL.of(url), orgId);
    }

    @SneakyThrows
    public static SoftwareStatementAssertion fromJWTJson(String jwtJson) {
        final String[] jwtEncodedParts = jwtJson.split("\\.");
        if (jwtEncodedParts.length != 3) {
            throw new IllegalArgumentException("JWT is not having 3 parts");
        }

        if (Strings.isNullOrEmpty(jwtEncodedParts[1])) {
            throw new IllegalArgumentException("JWT Payload is empty");
        }

        final String jwtPayload = new String(Base64.getDecoder().decode(jwtEncodedParts[1]));
        final JsonNode jwtPayloadNode = new ObjectMapper().readTree(jwtPayload);
        final String softwareId =
                getFirstFieldOrNull(jwtPayloadNode, OpenIdConstants.Params.SOFTWARE_ID);
        final String url =
                getFirstFieldOrNull(
                        jwtPayloadNode,
                        OpenIdConstants.Params.SOFTWARE_JWKS_ENDPOINT,
                        Params.ORG_JWKS_ENDPOINT);
        final String orgId = getFirstFieldOrNull(jwtPayloadNode, OpenIdConstants.Params.ORG_ID);

        return new SoftwareStatementAssertion(softwareId, url != null ? URL.of(url) : null, orgId);
    }

    private static String getFirstFieldOrNull(JsonNode jsonNode, String... fieldNames) {
        for (String fieldName : fieldNames) {
            String firstFieldOrNull = getFirstFieldOrNull(jsonNode, fieldName);
            if (firstFieldOrNull != null) {
                return firstFieldOrNull;
            }
        }

        return null;
    }

    private static String getFirstFieldOrNull(JsonNode jsonNode, String fieldName) {
        return Optional.ofNullable(jsonNode)
                .map(node -> node.get(fieldName))
                .map(JsonNode::asText)
                .orElse(null);
    }
}
