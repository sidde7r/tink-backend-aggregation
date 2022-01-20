package se.tink.backend.aggregation.startupchecks;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceInternalClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

@Slf4j
public class TppSecretsServiceHealthCheck implements HealthCheck {

    private final ManagedTppSecretsServiceInternalClient managedTppSecretsServiceInternalClient;

    // Used to fake a startup probe, we wait for this to be true one first time and then throw an
    // exception again if we fail
    private boolean firstCheckPassed;

    @Inject
    public TppSecretsServiceHealthCheck(
            ManagedTppSecretsServiceInternalClient managedTppSecretsServiceInternalClient) {
        this.managedTppSecretsServiceInternalClient = managedTppSecretsServiceInternalClient;
    }

    @Override
    public void check() throws NotHealthyException {
        if (!firstCheckPassed) {
            log.info("TppSecretsServiceHealthCheck has not passed yet.");
        }
        try {
            managedTppSecretsServiceInternalClient.ping();
        } catch (Exception e) {
            if (!firstCheckPassed) {
                throw new NotHealthyException("TppSecretsServiceHealthCheck failed", e);
            } else {
                log.warn("TppSecretsServiceHealthCheck failed", e);
            }
        }
        if (!firstCheckPassed) {
            firstCheckPassed = true;
            log.info("TppSecretsServiceHealthCheck passed.");
        }
    }
}
