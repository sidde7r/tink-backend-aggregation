package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import com.nimbusds.jose.crypto.RSASSASigner;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.configuration.UkOpenBankingConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@RequiredArgsConstructor
public class SecretServiceJwtSigner implements JwtSigner {

    private final UkOpenBankingConfiguration configuration;

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        RSASSASigner signer =
                new RSASSASigner(
                        RSA.getPrivateKeyFromBytes(
                                EncodingUtils.decodeBase64String(configuration.getSigningKey())));
        return TinkJwtSigner.builder(configuration::getSigningKeyId, signer)
                .withAlgorithm(algorithm)
                .withHeaderClaims(headerClaims)
                .withPayloadClaims(payloadClaims)
                .withDetachedPayload(detachedPayload)
                .build()
                .sign();
    }
}
