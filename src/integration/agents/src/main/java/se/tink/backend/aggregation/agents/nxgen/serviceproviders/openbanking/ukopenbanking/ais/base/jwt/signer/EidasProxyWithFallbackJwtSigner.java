package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.jwt.signer;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;

/**
 * Temporary solution till the migration to the eIDAS-Proxy won't be fully completed. We are waiting
 * full implementation which will be provided by TPA team- the fallback for UK OpenBanking certId
 */
@AllArgsConstructor
@Slf4j
// TODO remember to delete this class after migration
public class EidasProxyWithFallbackJwtSigner implements JwtSigner {

    private final EidasProxyJwtSigner eidasProxyJwtSigner;

    private final SecretServiceJwtSigner secretServiceJwtSigner;

    @Override
    public String sign(
            Algorithm algorithm,
            Map<String, Object> headerClaims,
            Map<String, Object> payloadClaims,
            boolean detachedPayload) {
        try {
            return eidasProxyJwtSigner.sign(
                    algorithm, headerClaims, payloadClaims, detachedPayload);
        } catch (RuntimeException e) {
            log.warn(
                    "Couldn't sign JWT using eIDAS-Proxy. Run fallback with legacy signing process. Cause: \n{}",
                    ExceptionUtils.getStackTrace(e));
            return secretServiceJwtSigner.sign(
                    algorithm, headerClaims, payloadClaims, detachedPayload);
        }
    }
}
