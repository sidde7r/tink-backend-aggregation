package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.fake;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface.JwtSigner;

public final class FakeJwtSigner implements JwtSigner {

    private static final String DEFAULT_SIGNATURE = "RkFLRV9TSUdOQVRVUkUK"; // b64(FAKE_SIGNATURE)

    private final String signature;

    public FakeJwtSigner() {
        this.signature = DEFAULT_SIGNATURE;
    }

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        return signature;
    }
}
