package se.tink.backend.aggregation.nxgen.controllers.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.Provider.AccessType;
import se.tink.backend.agents.rpc.ProviderTypes;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClient;

public final class AgentConfigurationController {

    private static final Logger log = LoggerFactory.getLogger(AgentConfigurationController.class);
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final TppSecretsServiceClient tppSecretsServiceClient;
    private final IntegrationsConfiguration integrationsConfiguration;
    private final boolean tppSecretsServiceEnabled;
    private final String financialInstitutionId;
    private final String appId;
    private final String clusterId;
    private final String redirectUrl;
    private final boolean isOpenBankingAgent;
    private final boolean isTestProvider;
    private Map<String, String> allSecrets;

    public AgentConfigurationController(
            TppSecretsServiceClient tppSecretsServiceClient,
            IntegrationsConfiguration integrationsConfiguration,
            Provider provider,
            String appId,
            String clusterId,
            String redirectUrl) {
        Preconditions.checkNotNull(
                tppSecretsServiceClient, "tppSecretsServiceClient cannot be null.");
        Preconditions.checkNotNull(provider, "provider cannot be null.");
        Preconditions.checkNotNull(
                Strings.emptyToNull(provider.getFinancialInstitutionId()),
                "financialInstitutionId cannot be empty/null.");
        Preconditions.checkNotNull(
                provider.getAccessType(), "provider.getAccessType() cannot be null.");
        Preconditions.checkNotNull(provider.getType(), "provider.getType() cannot be null.");
        Preconditions.checkNotNull(
                Strings.emptyToNull(clusterId), "clusterId cannot be empty/null.");

        // TODO: Enable precondiction and remove logging when verified by Access team that we don't
        //  get empty or null appIds.
        // Preconditions.checkNotNull(Strings.emptyToNull(appId), "appId cannot be empty/null");
        if (Strings.emptyToNull(appId) == null) {
            log.warn("appId cannot be empty/null for clusterId : " + clusterId);
        }

        this.tppSecretsServiceEnabled = tppSecretsServiceClient.isEnabled();
        this.tppSecretsServiceClient = tppSecretsServiceClient;
        if (!tppSecretsServiceEnabled) {
            Preconditions.checkNotNull(
                    integrationsConfiguration,
                    "integrationsConfiguration cannot be null if tppSecretsService is not enabled.");
        }
        this.integrationsConfiguration = integrationsConfiguration;
        this.financialInstitutionId = provider.getFinancialInstitutionId();
        this.appId = appId;
        this.clusterId = clusterId;
        this.redirectUrl = redirectUrl;
        this.isOpenBankingAgent = AccessType.OPEN_BANKING == provider.getAccessType();
        this.isTestProvider = ProviderTypes.TEST == provider.getType();

        if (isTestProvider) {
            log.info("Test provider, will not try to read agent configuration from SS.");
        }
    }

    public boolean isOpenBankingAgent() {
        return isOpenBankingAgent;
    }

    public boolean init() {
        if (tppSecretsServiceEnabled && isOpenBankingAgent && !isTestProvider) {
            try {
                Optional<Map<String, String>> allSecretsOpt =
                        tppSecretsServiceClient.getAllSecrets(
                                financialInstitutionId, appId, clusterId);

                // TODO: Remove if once Access team confirms there are no null appIds around.
                if (!allSecretsOpt.isPresent()) {
                    log.warn(
                            "Could not fetch secrets due to null or empty appId/financialInstitutionId");
                    return true;
                }

                allSecrets = allSecretsOpt.get();
            } catch (RuntimeException e) {
                if (e instanceof StatusRuntimeException) {
                    StatusRuntimeException statusRuntimeException = (StatusRuntimeException) e;
                    Preconditions.checkNotNull(
                            statusRuntimeException.getStatus(),
                            "Cannot be null Status for StatusRuntimeException: "
                                    + statusRuntimeException);
                    if (statusRuntimeException.getStatus().getCode()
                            == Status.NOT_FOUND.getCode()) {
                        log.info("Could not find secrets" + getSecretsServiceParamsString());
                        return true;
                    } else {
                        log.error(
                                "StatusRuntimeException when trying to retrieve secrets"
                                        + getSecretsServiceParamsString()
                                        + "with Status: "
                                        + statusRuntimeException.getStatus(),
                                statusRuntimeException);
                    }
                } else {
                    log.error("Error when retrieving secrets from Secrets Service.", e);
                }
                return false;
            }
            return initRedirectUrl();
        }
        return true;
    }

    private String getSecretsServiceParamsString() {
        return " for financialInstitutionId: "
                + financialInstitutionId
                + ", appId: "
                + appId
                + " and clusterId: "
                + clusterId
                + " ";
    }

    private boolean initRedirectUrl() {
        if (allSecrets == null) {
            log.error(
                    "allSecrets is null, make sure you fetched the secrets before you called initRedirectUrl.");
            return false;
        }

        final String REDIRECT_URLS_KEY = "redirectUrls";
        final String CHOSEN_REDIRECT_URL_KEY = "redirectUrl";

        if (!allSecrets.containsKey(REDIRECT_URLS_KEY)) {
            // We end up here when the secrets do not contain redirectUrls key.
            log.error("Could not find redirectUrls in secrets " + getSecretsServiceParamsString());

            return false;
        }

        Type listType = new TypeToken<List<String>>() {}.getType();

        final List<String> redirectUrls;
        try {
            redirectUrls = new Gson().fromJson(allSecrets.get(REDIRECT_URLS_KEY), listType);
        } catch (JsonSyntaxException e) {
            log.error(
                    "Could not parse redirectUrls secret : "
                            + allSecrets.get(REDIRECT_URLS_KEY)
                            + getSecretsServiceParamsString(),
                    e);
            return false;
        }

        if (redirectUrls.isEmpty()) {
            // We end up here when the secrets do contain redirectUrls key but it is an empty list.
            log.info("Empty redirectUrls list in secrets" + getSecretsServiceParamsString());

            return true;
        }

        if (Strings.isNullOrEmpty(redirectUrl)) {
            // No redirectUrl provided in the CredentialsRequest, pick the first one from
            // the registered list.
            allSecrets.put(CHOSEN_REDIRECT_URL_KEY, redirectUrls.get(0));
        } else if (!redirectUrls.contains(redirectUrl)) {
            // The redirectUrl provided in the CredentialsRequest is not among those
            // registered.
            log.error(
                    "Requested redirectUrl : "
                            + redirectUrl
                            + " is not registered"
                            + getSecretsServiceParamsString());
            return false;
        } else {
            // The redirectUrl provided in the CredentialsRequest is among those registered.
            allSecrets.put(CHOSEN_REDIRECT_URL_KEY, redirectUrl);
        }

        // To avoid agents accessing the list of registered redirectUrls via their configuration
        // classes. Declaring a member 'redirectUrls' for example.
        allSecrets.remove(REDIRECT_URLS_KEY);

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
                                                + " is missing"
                                                + getSecretsServiceParamsString()));
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
                                                + " is missing"
                                                + getSecretsServiceParamsString()
                                                + ". In the development.yml file."));
    }
}
