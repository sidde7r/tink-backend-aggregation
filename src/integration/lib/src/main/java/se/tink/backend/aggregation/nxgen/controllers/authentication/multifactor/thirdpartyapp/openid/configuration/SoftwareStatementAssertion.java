package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.Params;

@JsonObject
public class SoftwareStatementAssertion {

    private final String softwareId;

    private SoftwareStatementAssertion(String softwareId) {
        this.softwareId = softwareId;
    }

    public static SoftwareStatementAssertion fromJWT(String jwt) {
        final DecodedJWT decodedSsa = JWT.decode(jwt);
        String softwareId = decodedSsa.getClaim(Params.SOFTWARE_ID).asString();
        return new SoftwareStatementAssertion(softwareId);
    }

    @lombok.SneakyThrows
    public static SoftwareStatementAssertion fromJson(String json) {
        String softwareId = new ObjectMapper().readTree(json).get("software_id").asText();
        return new SoftwareStatementAssertion(softwareId);
    }

    public String getSoftwareId() {
        return softwareId;
    }
}
