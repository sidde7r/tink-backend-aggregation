package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface;

import java.util.Map;

public interface JwtSigner {

    String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload);

    enum Algorithm {
        RS256,
        PS256
    }
}
