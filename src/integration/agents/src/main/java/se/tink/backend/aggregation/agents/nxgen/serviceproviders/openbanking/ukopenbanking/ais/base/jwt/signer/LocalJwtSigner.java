package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import com.nimbusds.jose.crypto.RSASSASigner;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

@RequiredArgsConstructor
public class LocalJwtSigner implements JwtSigner {

    private final String signingKey;
    private final String signingKeyId;

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        RSASSASigner signer =
                new RSASSASigner(
                        RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(signingKey)));
        return TinkJwtSigner.builder(() -> signingKeyId, signer)
                .withAlgorithm(algorithm)
                .withHeaderClaims(headerClaims)
                .withPayloadClaims(payloadClaims)
                .withDetachedPayload(detachedPayload)
                .build()
                .sign();
    }
}
