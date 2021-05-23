package se.tink.backend.aggregation.startupchecks;

import com.google.inject.Inject;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidasidentity.identity.EidasIdentity;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

public class EidasProxySignerHealthCheck implements HealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(EidasProxySignerHealthCheck.class);

    private final EidasProxyConfiguration eidasProxyConfiguration;

    // Used to fake a startup probe, we wait for this to be true one first time and then throw an
    // exception again if we fail
    private boolean firstCheckPassed = false;

    @Inject
    public EidasProxySignerHealthCheck(EidasProxyConfiguration eidasProxyConfiguration) {
        this.eidasProxyConfiguration = eidasProxyConfiguration;
    }

    @Override
    public void check() throws NotHealthyException {
        if (!firstCheckPassed) {
            logger.info("EidasProxySignerHealthCheck has not passed yet.");
        }
        try {
            QsealcSigner signer =
                    QsealcSignerImpl.build(
                            eidasProxyConfiguration.toInternalConfig(),
                            QsealcAlg.EIDAS_JWT_RSA_SHA256,
                            new EidasIdentity("healthcheck", "healthcheck", "healthcheck","healthcheck"));
            signer.getSignatureBase64(Base64.getEncoder().encode("healthcheck".getBytes()));
        } catch (Exception e) {
            if (!firstCheckPassed) {
                throw new NotHealthyException("EidasProxySignerHealthCheck failed", e);
            } else {
                logger.warn("EidasProxySignerHealthCheck failed", e);
            }
        }
        if (!firstCheckPassed) {
            firstCheckPassed = true;
            logger.info("EidasProxySignerHealthCheck passed.");
        }
    }
}
