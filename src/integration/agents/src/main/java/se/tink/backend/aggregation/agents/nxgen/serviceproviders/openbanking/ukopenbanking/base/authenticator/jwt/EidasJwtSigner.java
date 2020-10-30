package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt;

import java.util.Map;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.jwt.kid.KidProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

public class EidasJwtSigner implements JwtSigner {
    private final EidasJwsSigner signer;
    private final KidProvider kidProvider;

    public EidasJwtSigner(KidProvider kidProvider, EidasJwsSigner signer) {
        this.kidProvider = kidProvider;
        this.signer = signer;
    }

    @Override
    @SneakyThrows
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        return TinkJwtSigner.builder(kidProvider, signer)
                .withAlgorithm(algorithm)
                .withHeaderClaims(headerClaims)
                .withPayloadClaims(payloadClaims)
                .withDetachedPayload(detachedPayload)
                .build()
                .sign();
    }
}
