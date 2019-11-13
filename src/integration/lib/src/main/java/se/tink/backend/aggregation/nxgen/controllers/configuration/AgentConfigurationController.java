package se.tink.backend.aggregation.nxgen.controllers.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.Provider.AccessType;
import se.tink.backend.agents.rpc.ProviderTypes;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.integration.tpp_secrets_service.client.TppSecretsServiceClient;
import se.tink.libraries.serialization.utils.JsonFlattener;

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
    private Set<String> secretValues = Collections.emptySet();
    private final Subject<Collection<String>> secretValuesSubject =
            BehaviorSubject.<Collection<String>>create().toSerialized();

    // Package private for testing purposes.
    AgentConfigurationController() {
        isOpenBankingAgent = false;
        isTestProvider = false;
        redirectUrl = null;
        clusterId = null;
        appId = null;
        financialInstitutionId = null;
        tppSecretsServiceEnabled = false;
        integrationsConfiguration = null;
        tppSecretsServiceClient = null;
    }

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
        Preconditions.checkNotNull(provider.getName(), "provider.getName() cannot be null.");
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
            log.info(
                    "Test provider : "
                            + provider.getName()
                            + " when reading secrets"
                            + getSecretsServiceParamsString()
                            + ", will not try to read agent configuration from SS.");
        }

        initSecrets();
    }

    public Observable<Collection<String>> getSecretValuesObservable() {
        return secretValuesSubject.subscribeOn(Schedulers.trampoline());
    }

    public void completeSecretValuesSubject() {
        secretValuesSubject.onComplete();
    }

    public boolean isOpenBankingAgent() {
        return isOpenBankingAgent;
    }

    private void initSecrets() {
        if (tppSecretsServiceEnabled && isOpenBankingAgent && !isTestProvider) {
            try {
                Optional<Map<String, String>> allSecretsOpt =
                        tppSecretsServiceClient.getAllSecrets(
                                financialInstitutionId, appId, clusterId);

                // TODO: Remove if once Access team confirms there are no null appIds around.
                if (!allSecretsOpt.isPresent()) {
                    log.warn(
                            "Could not fetch secrets due to null or empty appId/financialInstitutionId");
                }

                allSecrets = allSecretsOpt.get();
            } catch (StatusRuntimeException e) {
                Preconditions.checkNotNull(
                        e.getStatus(), "Status cannot be null for StatusRuntimeException: " + e);
                if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                    log.info("Could not find secrets" + getSecretsServiceParamsString());
                } else {
                    log.error(
                            "StatusRuntimeException when trying to retrieve secrets"
                                    + getSecretsServiceParamsString()
                                    + "with Status: "
                                    + e.getStatus(),
                            e);
                    throw e;
                }
            }
            initRedirectUrl();
            notifySecretValues(Sets.newHashSet(allSecrets.values()));
        }
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

    private void notifySecretValues(Set<String> newSecretValues) {
        newSecretValues.remove(null);
        if (!secretValues.containsAll(newSecretValues)) {
            Set<String> oldSecretValues = ImmutableSet.copyOf(secretValues);
            this.secretValues =
                    ImmutableSet.<String>builder()
                            .addAll(oldSecretValues)
                            .addAll(newSecretValues)
                            .build();

            secretValuesSubject.onNext(newSecretValues);
        }
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

    private void initRedirectUrl() {
        Preconditions.checkNotNull(
                allSecrets,
                "allSecrets is null, make sure you fetched the secrets before you called initRedirectUrl.");

        final String REDIRECT_URLS_KEY = "redirectUrls";
        final String CHOSEN_REDIRECT_URL_KEY = "redirectUrl";

        if (!allSecrets.containsKey(REDIRECT_URLS_KEY)) {
            // We end up here when the secrets do not contain redirectUrls key.
            throw new IllegalStateException(
                    "Could not find redirectUrls in secrets " + getSecretsServiceParamsString());
        }

        Type listType = new TypeToken<List<String>>() {}.getType();

        final List<String> redirectUrls;
        try {
            redirectUrls = new Gson().fromJson(allSecrets.get(REDIRECT_URLS_KEY), listType);
        } catch (JsonSyntaxException e) {
            throw new IllegalStateException(
                    "Could not parse redirectUrls secret : "
                            + allSecrets.get(REDIRECT_URLS_KEY)
                            + getSecretsServiceParamsString(),
                    e);
        }

        if (redirectUrls.isEmpty()) {
            // We end up here when the secrets do contain redirectUrls key but it is an empty list.
            log.info("Empty redirectUrls list in secrets" + getSecretsServiceParamsString());

            return;
        }

        if (Strings.isNullOrEmpty(redirectUrl)) {
            // No redirectUrl provided in the CredentialsRequest, pick the first one from
            // the registered list.
            allSecrets.put(CHOSEN_REDIRECT_URL_KEY, redirectUrls.get(0));
        } else if (!redirectUrls.contains(redirectUrl)) {
            // The redirectUrl provided in the CredentialsRequest is not among those
            // registered.
            throw new IllegalArgumentException(
                    "Requested redirectUrl : "
                            + redirectUrl
                            + " is not registered"
                            + getSecretsServiceParamsString());
        } else {
            // The redirectUrl provided in the CredentialsRequest is among those registered.
            allSecrets.put(CHOSEN_REDIRECT_URL_KEY, redirectUrl);
        }

        // To avoid agents accessing the list of registered redirectUrls via their configuration
        // classes. Declaring a member 'redirectUrls' for example.
        allSecrets.remove(REDIRECT_URLS_KEY);
    }

    public <T extends ClientConfiguration> T getAgentConfigurationFromK8s(
            String integrationName, String clientName, Class<T> clientConfigClass) {

        if (isOpenBankingAgent) {
            log.warn(
                    "Trying to read information from k8s for an OB agent: "
                            + clientConfigClass.toString()
                            + ". Consider uploading the configuration to ESS instead.");
        }

        Object clientConfigurationAsObject =
                integrationsConfiguration
                        .getClientConfigurationAsObject(integrationName, clientName)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Agent configuration for agent: "
                                                        + clientConfigClass.toString()
                                                        + " not found in k8s secrets of cluster: "
                                                        + clusterId));

        extractSensitiveValues(clientConfigurationAsObject);

        return OBJECT_MAPPER.convertValue(clientConfigurationAsObject, clientConfigClass);
    }

    public <T extends ClientConfiguration> T getAgentConfigurationFromK8s(
            String integrationName, Class<T> clientConfigClass) {
        if (isOpenBankingAgent) {
            log.warn(
                    "Trying to read information from k8s for an OB agent: "
                            + clientConfigClass.toString()
                            + ". Consider uploading the configuration to ESS instead.");
        }

        Object clientConfigurationAsObject =
                integrationsConfiguration
                        .getIntegration(integrationName)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Agent configuration for agent: "
                                                        + clientConfigClass.toString()
                                                        + " not found in k8s secrets of cluster: "
                                                        + clusterId));

        extractSensitiveValues(clientConfigurationAsObject);

        return OBJECT_MAPPER.convertValue(clientConfigurationAsObject, clientConfigClass);
    }

    public <T extends ClientConfiguration> Optional<T> getAgentConfigurationFromK8sAsOptional(
            String integrationName, Class<T> clientConfigClass) {
        if (isOpenBankingAgent) {
            log.warn(
                    "Trying to read information from k8s for an OB agent: "
                            + clientConfigClass.toString()
                            + ". Consider uploading the configuration to ESS instead.");
        }

        Optional<Object> clientConfigurationAsObject =
                integrationsConfiguration.getIntegration(integrationName);

        clientConfigurationAsObject.ifPresent(
                clientConfiguration -> extractSensitiveValues(clientConfiguration));

        return clientConfigurationAsObject.map(
                clientConfiguration ->
                        OBJECT_MAPPER.convertValue(clientConfiguration, clientConfigClass));
    }

    <T extends ClientConfiguration> Set<String> extractSensitiveValues(
            Object clientConfigurationAsObject) {

        final Map<String, String> sensitiveValuesMap;
        try {
            sensitiveValuesMap = JsonFlattener.flattenJsonToMap(clientConfigurationAsObject);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Unexpected error when extracting sensitive agent configuration values.");
        }

        Set<String> extractedSensitiveValues = Sets.newHashSet(sensitiveValuesMap.values());
        notifySecretValues(extractedSensitiveValues);

        return extractedSensitiveValues;
    }

    // Used to read agent configuration from development.yml instead of Secrets Service
    private <T extends ClientConfiguration> T getAgentConfigurationDev(
            final Class<T> clientConfigClass) {
        log.info(
                "Not reading agent configuration from Secrets Service, make sure that you have "
                        + "uploaded the configuration to Secrets Service in staging and/or "
                        + "production.");

        Object clientConfigurationAsObject =
                integrationsConfiguration
                        .getClientConfigurationAsObject(financialInstitutionId, appId)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Agent configuration for agent: "
                                                        + clientConfigClass.toString()
                                                        + " is missing"
                                                        + getSecretsServiceParamsString()
                                                        + ". In the development.yml file."));

        extractSensitiveValues(clientConfigurationAsObject);

        return OBJECT_MAPPER.convertValue(clientConfigurationAsObject, clientConfigClass);
    }
}
