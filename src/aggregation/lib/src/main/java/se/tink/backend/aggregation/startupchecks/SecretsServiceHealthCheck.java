package se.tink.backend.aggregation.startupchecks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.backend.libraries.healthcheckhandler.HealthCheck;
import se.tink.backend.libraries.healthcheckhandler.NotHealthyException;

public class SecretsServiceHealthCheck implements HealthCheck {

    private TppSecretsServiceClient tppSecretsServiceClient;

    private static final Logger logger = LoggerFactory.getLogger(SecretsServiceHealthCheck.class);

    public SecretsServiceHealthCheck(ManagedTppSecretsServiceClient tppSecretsServiceClient) {
        this.tppSecretsServiceClient = tppSecretsServiceClient;
    }

    @Override
    public void check() throws NotHealthyException {
        try {
            tppSecretsServiceClient.ping();
        } catch (Exception e) {
            logger.error("Exception when performing SecretsService health check");
            throw new NotHealthyException(e);
        }
    }
}
