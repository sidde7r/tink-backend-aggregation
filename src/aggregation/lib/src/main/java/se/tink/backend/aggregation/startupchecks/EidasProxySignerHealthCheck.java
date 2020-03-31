package se.tink.backend.aggregation.startupchecks;

import com.google.inject.Inject;
import java.util.Base64;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

public class EidasProxySignerHealthCheck implements HealthCheck {

    private final EidasProxyConfiguration eidasProxyConfiguration;

    @Inject
    public EidasProxySignerHealthCheck(EidasProxyConfiguration eidasProxyConfiguration) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
    }

    @Override
    public void check() throws NotHealthyException {
        try {
            QsealcSigner signer =
                    QsealcSigner.build(
                            eidasProxyConfiguration.toInternalConfig(),
                            QsealcAlg.EIDAS_JWT_RSA_SHA256,
                            new EidasIdentity("healthcheck", "healthcheck", "healthcheck"),
                            "healthcheck");
            signer.getSignatureBase64(Base64.getEncoder().encode("healthcheck".getBytes()));
        } catch (Exception e) {
            throw new NotHealthyException("EidasProxySignerHealthCheck failed", e);
        }
    }
}
