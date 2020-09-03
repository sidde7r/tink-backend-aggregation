package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.Params;

@JsonObject
public class SoftwareStatementAssertion {

    private DecodedJWT decodedSsa;

    public SoftwareStatementAssertion(String assertion) {
        this.decodedSsa = JWT.decode(assertion);
    }

    public String getSoftwareId() {
        return decodedSsa.getClaim(Params.SOFTWARE_ID).asString();
    }
}
