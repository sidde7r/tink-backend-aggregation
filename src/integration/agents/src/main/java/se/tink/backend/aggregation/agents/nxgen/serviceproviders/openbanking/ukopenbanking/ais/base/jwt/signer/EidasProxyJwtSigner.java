package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.kid.KeyIdProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

@RequiredArgsConstructor
public class EidasProxyJwtSigner implements JwtSigner {

    private final KeyIdProvider keyIdProvider;
    private final EidasJwsSigner signer;

    @Override
    @SneakyThrows
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        return TinkJwtSigner.builder(keyIdProvider, signer)
                .withAlgorithm(algorithm)
                .withHeaderClaims(headerClaims)
                .withPayloadClaims(payloadClaims)
                .withDetachedPayload(detachedPayload)
                .build()
                .sign();
    }
}
