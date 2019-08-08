package se.tink.backend.aggregation.nxgen.controllers.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClient;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceConfiguration;

public final class AgentConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(AgentConfigurationController.class);
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final TppSecretsServiceClient tppSecretsServiceClient;
    private final IntegrationsConfiguration integrationsConfiguration;
    private final boolean tppSecretsServiceEnabled;
    private final String financialInstitutionId;
    private final String appId;
    private Map<String, String> allSecrets;

    public AgentConfigurationController(
            TppSecretsServiceConfiguration tppSecretsServiceConfiguration,
            IntegrationsConfiguration integrationsConfiguration,
            String financialInstitutionId,
            String appId) {
        Preconditions.checkNotNull(
                tppSecretsServiceConfiguration, "tppSecretsServiceConfiguration not found.");
        Preconditions.checkNotNull(
                Strings.emptyToNull(financialInstitutionId),
                "financialInstitutionId cannot be empty/null.");
        Preconditions.checkNotNull(Strings.emptyToNull(appId), "appId cannot be empty/null");

        this.tppSecretsServiceEnabled = tppSecretsServiceConfiguration.isEnabled();
        if (tppSecretsServiceEnabled) {
            this.tppSecretsServiceClient =
                    new TppSecretsServiceClient(tppSecretsServiceConfiguration);
        } else {
            Preconditions.checkNotNull(
                    integrationsConfiguration,
                    "integrationsConfiguration cannot be null if tppSecretsService is not enabled.");
            this.tppSecretsServiceClient = null;
        }
        this.integrationsConfiguration = integrationsConfiguration;
        this.financialInstitutionId = financialInstitutionId;
        this.appId = appId;
    }

    public boolean init() {
        if (tppSecretsServiceEnabled) {
            try {
                allSecrets = tppSecretsServiceClient.getAllSecrets(financialInstitutionId, appId);
            } catch (RuntimeException e) {
                if (e instanceof StatusRuntimeException) {
                    StatusRuntimeException statusRuntimeException = (StatusRuntimeException) e;
                    if (statusRuntimeException.getStatus() == Status.NOT_FOUND) {
                        log.info(
                                "Could not find secrets for financialInsitutionId: "
                                        + financialInstitutionId
                                        + " and appId: "
                                        + appId);
                        return true;
                    } else {
                        log.error(
                                "StatusRuntimeException when trying to retrieve secrets for financialInstitutionId: "
                                        + financialInstitutionId
                                        + " and appId: "
                                        + appId
                                        + " with Status: "
                                        + statusRuntimeException.getStatus(),
                                statusRuntimeException);
                    }
                } else {
                    log.error("Error when retrieving secrets from Secrets Service.", e);
                }
                return false;
            }
        }
        return true;
    }

    public <T extends ClientConfiguration> T getAgentConfiguration(
            final Class<T> clientConfigClass) {

        // For local development we can use the development.yml file.
        if (!tppSecretsServiceEnabled) {
            return getAgentConfigurationDev(clientConfigClass);
        }

        Preconditions.checkNotNull(
                allSecrets,
                "Secrets were not fetched. Try to init() the AgentConfigurationController.");

        T clientConfig = OBJECT_MAPPER.convertValue(allSecrets, clientConfigClass);

        return Optional.ofNullable(clientConfig)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Agent configuration for agent: "
                                                + clientConfigClass.toString()
                                                + " is missing for financialInstitutionId: "
                                                + financialInstitutionId
                                                + " and appId: "
                                                + appId));
    }

    // Used to read agent configuration from development.yml instead of Secrets Service
    private <T extends ClientConfiguration> T getAgentConfigurationDev(
            final Class<T> clientConfigClass) {
        log.warn(
                "Not reading agent configuration from Secrets Service, make sure that you have "
                        + "uploaded the configuration to Secrets Service in staging and/or "
                        + "production.");

        return integrationsConfiguration
                .getClientConfiguration(financialInstitutionId, appId, clientConfigClass)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Agent configuration for agent: "
                                                + clientConfigClass.toString()
                                                + " is missing for financialInstitutionId: "
                                                + financialInstitutionId
                                                + " and appId: "
                                                + appId
                                                + ". In the development.yml file."));
    }
}
