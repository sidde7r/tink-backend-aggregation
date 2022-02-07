package se.tink.backend.aggregation.startupchecks;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceInternalClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

public class SecretsServiceHealthCheck implements HealthCheck {

    private static final Logger logger = LoggerFactory.getLogger(SecretsServiceHealthCheck.class);

    private final ManagedTppSecretsServiceInternalClient managedTppSecretsServiceInternalClient;

    // Used to fake a startup probe, we wait for this to be true one first time and then throw an
    // exception again if we fail
    private boolean firstCheckPassed = false;

    @Inject
    public SecretsServiceHealthCheck(
            ManagedTppSecretsServiceInternalClient managedTppSecretsServiceInternalClient) {
        this.managedTppSecretsServiceInternalClient = managedTppSecretsServiceInternalClient;
    }

    @Override
    public void check() throws NotHealthyException {
        if (!firstCheckPassed) {
            logger.info("SecretsServiceHealthCheck has not passed yet.");
        }
        try {
            logger.info("ping from managedTppSecretsServiceInternalClient.");
            managedTppSecretsServiceInternalClient.ping();
        } catch (Exception e) {
            if (!firstCheckPassed) {
                throw new NotHealthyException("SecretsServiceHealthCheck failed", e);
            } else {
                logger.warn("SecretsServiceHealthCheck failed", e);
            }
        }
        if (!firstCheckPassed) {
            firstCheckPassed = true;
            logger.info("SecretsServiceHealthCheck passed.");
        }
    }
}
