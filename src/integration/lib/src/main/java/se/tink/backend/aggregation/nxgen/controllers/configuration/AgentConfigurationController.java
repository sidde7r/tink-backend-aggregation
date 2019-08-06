package se.tink.backend.aggregation.nxgen.controllers.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
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

    public AgentConfigurationController(
            TppSecretsServiceConfiguration tppSecretsServiceConfiguration,
            IntegrationsConfiguration integrationsConfiguration) {
        Preconditions.checkNotNull(
                tppSecretsServiceConfiguration, "tppSecretsServiceConfiguration not found.");

        this.tppSecretsServiceEnabled = tppSecretsServiceConfiguration.isEnabled();
        if (tppSecretsServiceEnabled) {
            this.tppSecretsServiceClient =
                    new TppSecretsServiceClient(tppSecretsServiceConfiguration);
        } else {
            this.tppSecretsServiceClient = null;
        }
        this.integrationsConfiguration = integrationsConfiguration;
    }

    public <T extends ClientConfiguration> T getAgentConfiguration(
            final String financialInstitutionId,
            final String appId,
            final Class<T> clientConfigClass) {

        // For local development we can use the development.yml file.
        if (!tppSecretsServiceEnabled) {
            return getAgentConfigurationDev(financialInstitutionId, appId, clientConfigClass);
        }

        Map<String, String> allSecrets =
                tppSecretsServiceClient.getAllSecrets(financialInstitutionId, appId);

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
            final String financialInstitutionId,
            final String appId,
            final Class<T> clientConfigClass) {
        log.warn(
                "Not reading agent configuration from Secrets Service, make sure that you have "
                        + "uploaded the configuration to Secrets Service in staging and/or "
                        + "production.");
        Preconditions.checkNotNull(
                integrationsConfiguration,
                "integrations configurations cannot be null if getAgentConfigurationDev is to be used.");

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
