package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt;

import com.nimbusds.jose.crypto.RSASSASigner;
import java.util.Map;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

public class LocalJwtSigner implements JwtSigner {

    private final String signingKey;
    private final String signingKeyId;

    public LocalJwtSigner(String signingKey, String signingKeyId) {
        this.signingKey = signingKey;
        this.signingKeyId = signingKeyId;
    }

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
