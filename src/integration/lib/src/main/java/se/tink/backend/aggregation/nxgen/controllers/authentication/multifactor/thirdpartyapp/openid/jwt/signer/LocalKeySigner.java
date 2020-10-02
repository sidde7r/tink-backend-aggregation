package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer;

import java.security.interfaces.RSAPrivateKey;
import java.util.Map;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

/** @deprecated Please use @{JwksKeySigner} */
@Deprecated
public class LocalKeySigner implements JwtSigner {

    private final String keyId;
    private final RSAPrivateKey signingKey;

    public LocalKeySigner(String keyId, RSAPrivateKey signingKey) {
        this.keyId = keyId;
        this.signingKey = signingKey;
    }

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {

        return TinkJwtSigner.builder(() -> keyId)
                .withAlgorithm(algorithm)
                .withHeaderClaims(headerClaims)
                .withPayloadClaims(payloadClaims)
                .withDetachedPayload(detachedPayload)
                .build()
                .sign(signingKey);
    }
}
