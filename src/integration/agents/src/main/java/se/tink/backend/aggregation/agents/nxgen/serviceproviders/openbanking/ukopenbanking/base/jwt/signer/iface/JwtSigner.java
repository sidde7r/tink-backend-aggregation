package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.jwt.signer.iface;

import java.util.Map;

public interface JwtSigner {

    enum Algorithm {
        RS256,
        PS256
    }

    String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload);
}
