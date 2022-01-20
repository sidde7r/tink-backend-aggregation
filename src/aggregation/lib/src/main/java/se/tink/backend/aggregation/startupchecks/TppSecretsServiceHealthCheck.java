package se.tink.backend.aggregation.startupchecks;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;
import se.tink.backend.secretsservice.client.SecretsServiceInternalClient;

@Slf4j
public class TppSecretsServiceHealthCheck implements HealthCheck {

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
            log.info("TppSecretsServiceHealthCheck has not passed yet.");
        }
        try {
            secretsServiceInternalClient.ping();
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
