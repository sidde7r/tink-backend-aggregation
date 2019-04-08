package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Preconditions;
import java.util.Date;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.JwtUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.OpenIdSignUtils;

public class ClientAssertion {

    public static class Builder {
        private WellKnownResponse wellknownConfiguration;
        private SoftwareStatement softwareStatement;
        private ClientInfo clientInfo;

        public Builder withSoftwareStatement(SoftwareStatement softwareStatement) {
            this.softwareStatement = softwareStatement;
            return this;
        }

        public Builder withWellknownConfiguration(WellKnownResponse wellknownConfiguration) {
            this.wellknownConfiguration = wellknownConfiguration;
            return this;
        }

        public Builder withClientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public String build() {
            Preconditions.checkNotNull(
                    wellknownConfiguration, "WellknownConfiguration must be specified.");
            Preconditions.checkNotNull(softwareStatement, "SoftwareStatement must be specified.");
            Preconditions.checkNotNull(clientInfo, "ClientInfo must be specified.");

            String keyId = softwareStatement.getSigningKeyId();
            Algorithm algorithm =
                    OpenIdSignUtils.getSignatureAlgorithm(softwareStatement.getSigningKey());

            String clientId = clientInfo.getClientId();
            String tokenEndpoint = wellknownConfiguration.getTokenEndpoint().toString();

            // Issued = Now, Expires = Now + 1h
            Date issuedAt = new Date();
            Date expiresAt = JwtUtils.addHours(issuedAt, 1);

            String jwtId = JwtUtils.generateId();

            return JWT.create()
                    .withKeyId(keyId)
                    .withJWTId(jwtId)
                    .withIssuer(clientId)
                    .withSubject(clientId)
                    .withAudience(tokenEndpoint)
                    .withIssuedAt(issuedAt)
                    .withExpiresAt(expiresAt)
                    .sign(algorithm);
        }
    }

    public static Builder create() {
        return new Builder();
    }
}
