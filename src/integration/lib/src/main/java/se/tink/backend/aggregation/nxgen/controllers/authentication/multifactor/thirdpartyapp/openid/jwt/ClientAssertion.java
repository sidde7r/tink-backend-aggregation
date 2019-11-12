package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.google.common.base.Preconditions;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils.JwtUtils;

public class ClientAssertion {

    public static class Builder {
        private WellKnownResponse wellknownConfiguration;
        private ClientInfo clientInfo;

        public Builder withWellknownConfiguration(WellKnownResponse wellknownConfiguration) {
            this.wellknownConfiguration = wellknownConfiguration;
            return this;
        }

        public Builder withClientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public String build(JwtSigner signer) {
            Preconditions.checkNotNull(
                    wellknownConfiguration, "WellknownConfiguration must be specified.");
            Preconditions.checkNotNull(clientInfo, "ClientInfo must be specified.");

            String clientId = clientInfo.getClientId();
            String tokenEndpoint = wellknownConfiguration.getTokenEndpoint().toString();

            // Issued = Now, Expires = Now + 1h
            // Issued = Now, Expires = Now + 1h
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plusSeconds(TimeUnit.HOURS.toSeconds(1));

            String jwtId = JwtUtils.generateId();

            return TinkJwt.create()
                    .withJWTId(jwtId)
                    .withIssuer(clientId)
                    .withSubject(clientId)
                    .withAudience(tokenEndpoint)
                    .withIssuedAt(issuedAt)
                    .withExpiresAt(expiresAt)
                    .signAttached(JwtSigner.Algorithm.RS256, signer);
        }
    }

    public static Builder create() {
        return new Builder();
    }
}
