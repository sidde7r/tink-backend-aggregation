package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Base64;
import java.util.HashMap;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.Params;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class SoftwareStatementAssertion {

    private String softwareId;

    public SoftwareStatementAssertion(String assertion) {
        try {
            final DecodedJWT decodedSsa = JWT.decode(assertion);
            this.softwareId = decodedSsa.getClaim(Params.SOFTWARE_ID).asString();
        } catch (JWTDecodeException e) {
            final String json = new String(Base64.getDecoder().decode(assertion));
            this.softwareId =
                    (String)
                            SerializationUtils.deserializeFromString(
                                            json, new TypeReference<HashMap<String, Object>>() {})
                                    .get("software_id");
        }
    }

    public String getSoftwareId() {
        return softwareId;
    }
}
