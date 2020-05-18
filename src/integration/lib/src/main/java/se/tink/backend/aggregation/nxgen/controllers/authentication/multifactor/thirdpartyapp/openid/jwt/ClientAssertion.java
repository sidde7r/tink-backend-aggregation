package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt;

import com.google.common.base.Preconditions;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.WellKnownResponse;

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

        public String build(JwtSigner signer, JwtSigner.Algorithm tokenEndpointAuthSigningAlg) {
            Preconditions.checkNotNull(
                    wellknownConfiguration, "WellknownConfiguration must be specified.");
            Preconditions.checkNotNull(clientInfo, "ClientInfo must be specified.");

            String clientId = clientInfo.getClientId();
            String tokenEndpoint = wellknownConfiguration.getTokenEndpoint().toString();

            // Issued = Now, Expires = Now + 1h
            // Issued = Now, Expires = Now + 1h
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plusSeconds(TimeUnit.MINUTES.toSeconds(5));

            String jwtId = EncodingUtils.encodeAsBase64String(RandomUtils.secureRandom(16));

            return TinkJwt.create()
                    .withJWTId(jwtId)
                    .withIssuer(clientId)
                    .withSubject(clientId)
                    .withAudience(tokenEndpoint)
                    .withIssuedAt(issuedAt)
                    .withExpiresAt(expiresAt)
                    .signAttached(tokenEndpointAuthSigningAlg, signer);
        }
    }

    public static Builder create() {
        return new Builder();
    }
}
