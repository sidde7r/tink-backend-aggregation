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
    // For fallback
    private boolean fallback = false;
    private String integrationName;
    private String clientName;

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
        // TODO: Enable preconditons once we get word from aggregation that all users have an appId.
        // Preconditions.checkNotNull(Strings.emptyToNull(appId), "appId cannot be empty/null");

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

    public AgentConfigurationController withFallback(String integrationName, String clientName) {
        this.fallback = true;
        this.integrationName = integrationName;
        this.clientName = clientName;
        return this;
    }

    public AgentConfigurationController withoutFallback() {
        this.fallback = false;
        this.integrationName = null;
        this.clientName = null;
        return this;
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
                                "Could not find secrets for financialInstitutionId: "
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

    public void shutdownTppSecretsServiceClient() {
        if (tppSecretsServiceEnabled) {
            Optional.ofNullable(tppSecretsServiceClient).ifPresent(client -> client.shutdown());
        }
    }

    public <T extends ClientConfiguration> T getAgentConfiguration(
            final Class<T> clientConfigClass) {

        // TODO: Remove once fallback is no longer needed
        if (fallback && allSecrets == null) {
            Preconditions.checkNotNull(
                    integrationName,
                    "integrationName cannot be null when using fallback to fetch secrets.");
            Preconditions.checkNotNull(
                    clientName, "clientName cannot be null when using fallback to fetch secrets.");
            return getAgentConfigurationFallback(integrationName, clientName, clientConfigClass);
        }

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

    private <T extends ClientConfiguration> T getAgentConfigurationFallback(
            String integrationName, String clientName, Class<T> clientConfigClass) {
        log.info("Falling back to k8s, try to use secrets service instead.");

        return integrationsConfiguration
                .getClientConfiguration(integrationName, clientName, clientConfigClass)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        "Agent configuration for agent: "
                                                + clientConfigClass.toString()
                                                + " is missing for integrationName: "
                                                + integrationName
                                                + " and clientName: "
                                                + clientName
                                                + ". In the development.yml file."));
    }

    // Used to read agent configuration from development.yml instead of Secrets Service
    private <T extends ClientConfiguration> T getAgentConfigurationDev(
            final Class<T> clientConfigClass) {
        log.info(
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
