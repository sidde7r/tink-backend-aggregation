package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.ClientInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.TinkJwt;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc.WellKnownResponse;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;

@Data
@Builder
@AllArgsConstructor
public class ClientAssertion {

    public static class Builder {
        private WellKnownResponse wellKnownConfiguration;
        private ClientInfo clientInfo;
        private String audience;
        private JwtSigner signer;
        private JwtSigner.Algorithm tokenEndpointAuthSigningAlg;

        public Builder withWellKnownConfiguration(WellKnownResponse wellKnownConfiguration) {
            this.wellKnownConfiguration = wellKnownConfiguration;
            return this;
        }

        public Builder withClientInfo(ClientInfo clientInfo) {
            this.clientInfo = clientInfo;
            return this;
        }

        public Builder withAudience(String audience) {
            this.audience = audience;
            return this;
        }

        public Builder withSigner(
                JwtSigner signer, JwtSigner.Algorithm tokenEndpointAuthSigningAlg) {
            this.signer = signer;
            this.tokenEndpointAuthSigningAlg = tokenEndpointAuthSigningAlg;
            return this;
        }

        public String build() {
            Preconditions.checkNotNull(
                    wellKnownConfiguration, "wellKnownConfiguration must be specified.");
            Preconditions.checkNotNull(clientInfo, "clientInfo must be specified.");
            Preconditions.checkNotNull(signer, "signer must be specified.");
            Preconditions.checkNotNull(
                    tokenEndpointAuthSigningAlg, "tokenEndpointAuthSigningAlg must be specified.");

            String jwtId = EncodingUtils.encodeAsBase64String(RandomUtils.secureRandom(16));
            String clientId = clientInfo.getClientId();
            if (Strings.isNullOrEmpty(audience)) {
                audience = wellKnownConfiguration.getTokenEndpoint().toString();
            }

            // Issued = Now, Expires = Now + 1h
            Instant issuedAt = Instant.now();
            Instant expiresAt = issuedAt.plusSeconds(TimeUnit.MINUTES.toSeconds(5));

            return TinkJwt.create()
                    .withJWTId(jwtId)
                    .withIssuer(clientId)
                    .withSubject(clientId)
                    .withAudience(audience)
                    .withIssuedAt(issuedAt)
                    .withExpiresAt(expiresAt)
                    .signAttached(tokenEndpointAuthSigningAlg, signer);
        }
    }

    public static Builder create() {

        return new Builder();
    }
}
