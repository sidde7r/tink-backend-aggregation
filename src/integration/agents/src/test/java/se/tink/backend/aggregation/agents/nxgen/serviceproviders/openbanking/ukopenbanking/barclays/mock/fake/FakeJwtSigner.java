package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.barclays.mock.fake;

import java.util.Map;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.jwt.signer.iface.JwtSigner;

public final class FakeJwtSigner implements JwtSigner {

    private static final String SIGNATURE = "RkFLRV9TSUdOQVRVUkUK"; // b64(FAKE_SIGNATURE)

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        return SIGNATURE;
    }
}
