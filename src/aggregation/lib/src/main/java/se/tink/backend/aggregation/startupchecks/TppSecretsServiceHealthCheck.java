package se.tink.backend.aggregation.startupchecks;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;
import se.tink.backend.secretsservice.client.SecretsServiceInternalClient;

public class TppSecretsServiceHealthCheck implements HealthCheck {

    private static final Logger logger =
            LoggerFactory.getLogger(TppSecretsServiceHealthCheck.class);

    private final SecretsServiceInternalClient secretsServiceInternalClient;

    // Used to fake a startup probe, we wait for this to be true one first time and then throw an
    // exception again if we fail
    private boolean firstCheckPassed;

    @Inject
    public TppSecretsServiceHealthCheck(SecretsServiceInternalClient secretsServiceInternalClient) {
        this.secretsServiceInternalClient = secretsServiceInternalClient;
    }

    @Override
    public void check() throws NotHealthyException {
        if (!firstCheckPassed) {
            logger.info("TppSecretsServiceHealthCheck has not passed yet.");
        }
        try {
            secretsServiceInternalClient.ping();
        } catch (Exception e) {
            if (!firstCheckPassed) {
                throw new NotHealthyException("TppSecretsServiceHealthCheck failed", e);
            } else {
                logger.warn("TppSecretsServiceHealthCheck failed", e);
            }
        }
        if (!firstCheckPassed) {
            firstCheckPassed = true;
            logger.info("TppSecretsServiceHealthCheck passed.");
        }
    }
}
