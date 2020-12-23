package se.tink.backend.aggregation.nxgen.controllers.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.Provider.AccessType;
import se.tink.backend.aggregation.configuration.IntegrationsConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.integration.tpp_secrets_service.client.entities.SecretsEntityCore;
import se.tink.backend.integration.tpp_secrets_service.client.iface.TppSecretsServiceClient;
import se.tink.libraries.provider.ProviderDto.ProviderTypes;
import se.tink.libraries.serialization.utils.JsonFlattener;

public final class AgentConfigurationController implements AgentConfigurationControllerable {
    private static final Logger log = LoggerFactory.getLogger(AgentConfigurationController.class);
    private static final ObjectMapper OBJECT_MAPPER =
            new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static final String TRY_READ_FROM_K8_LOGGER_MSG =
            "Trying to read information from k8s for an OB agent: {}. Consider uploading the configuration to ESS instead.";

    private final TppSecretsServiceClient tppSecretsServiceClient;
    private final IntegrationsConfiguration integrationsConfiguration;
    private final boolean tppSecretsServiceEnabled;
    private final String financialInstitutionId;
    private final String providerId;
    private final String appId;
    private final String clusterId;
    private final String redirectUrl;
    private final boolean isOpenBankingAgent;
    private final boolean isTestProvider;
    private Map<String, Object> allSecretsMapObj = new HashMap<>();
    private Set<String> secretValues = Collections.emptySet();
    private final Subject<Collection<String>> secretValuesSubject =
            BehaviorSubject.<Collection<String>>create().toSerialized();
    private static final String REDIRECT_URL_KEY = "redirectUrl";
    private static final String QWAC_KEY = "qwac";
    private static final String QSEALC_KEY = "qsealc";

    // Package private for testing purposes.
    AgentConfigurationController() {
        isOpenBankingAgent = false;
        isTestProvider = false;
        redirectUrl = null;
        clusterId = null;
        appId = null;
        financialInstitutionId = null;
        providerId = null;
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
                Strings.emptyToNull(provider.getName()), "providerId cannot be empty/null.");
        Preconditions.checkNotNull(
                provider.getAccessType(), "provider.getAccessType() cannot be null.");
        Preconditions.checkNotNull(provider.getType(), "provider.getType() cannot be null.");
        Preconditions.checkNotNull(
                Strings.emptyToNull(clusterId), "clusterId cannot be empty/null.");

        // TODO: Enable precondiction and remove logging when verified by Access team that we don't
        //  get empty or null appIds.
        // Preconditions.checkNotNull(Strings.emptyToNull(appId), "appId cannot be empty/null");
        if (Strings.emptyToNull(appId) == null) {
            log.warn("appId cannot be empty/null for clusterId: {}", clusterId);
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
        this.providerId = provider.getName();
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

    @Override
    public Observable<Collection<String>> getSecretValuesObservable() {
        return secretValuesSubject.subscribeOn(Schedulers.trampoline());
    }

    @Override
    public void completeSecretValuesSubject() {
        secretValuesSubject.onComplete();
    }

    @Override
    public boolean isOpenBankingAgent() {
        return isOpenBankingAgent;
    }

    private void initSecrets() {
        if (tppSecretsServiceEnabled && isOpenBankingAgent && !isTestProvider) {
            try {
                Optional<SecretsEntityCore> allSecretsOpt =
                        tppSecretsServiceClient.getAllSecrets(
                                financialInstitutionId, appId, clusterId, providerId);

                // TODO: Remove if once Access team confirms there are no null appIds around.
                if (!allSecretsOpt.isPresent()) {
                    log.warn(
                            "Could not fetch all secrets due to null or empty appId/financialInstitutionId");
                }

                SecretsEntityCore allSecrets = allSecretsOpt.get();
                Map<String, String> secretsMap = allSecrets.getSecrets();

                Preconditions.checkNotNull(
                        secretsMap,
                        "allSecrets is null, make sure you fetched the secrets before you called initRedirectUrl.");

                allSecretsMapObj =
                        secretsMap.entrySet().stream()
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                // This method needs to be called before the init methods because we only want to
                // mask the values from the secrets objects (secrets and sensitiveSecrets) and the
                // init method put other stuff in that map, i.e. selected redirectUrl and scopes. So
                // order is important.
                extractSensitiveValues(allSecretsMapObj);

                initRedirectUrl(allSecrets.getRedirectUrls());
                initScopes(allSecrets.getScopes());
                initCertsData(allSecrets.getQwac(), allSecrets.getQsealc());
            } catch (StatusRuntimeException e) {
                Preconditions.checkNotNull(
                        e.getStatus(), "Status cannot be null for StatusRuntimeException: " + e);
                if (e.getStatus().getCode() == Status.NOT_FOUND.getCode()) {
                    log.info("Could not find secrets {}", getSecretsServiceParamsString());
                } else {
                    log.error(
                            "StatusRuntimeException when trying to retrieve secrets {} with Status: {}",
                            getSecretsServiceParamsString(),
                            e.getStatus(),
                            e);
                    throw e;
                }
            }
        }
    }

    private void initCertsData(String qwac, String qsealc) {
        allSecretsMapObj.put(QWAC_KEY, Strings.nullToEmpty(qwac));
        allSecretsMapObj.put(QSEALC_KEY, Strings.nullToEmpty(qsealc));
    }

    private void initRedirectUrl(List<String> redirectUrls) {
        if (CollectionUtils.isEmpty(redirectUrls)) {
            throw new IllegalStateException(
                    "Could not find redirectUrls in secrets " + getSecretsServiceParamsString());
        }

        if (Strings.isNullOrEmpty(redirectUrl)) {
            // No redirectUrl provided in the CredentialsRequest, pick the first one from
            // the registered list.
            allSecretsMapObj.put(REDIRECT_URL_KEY, redirectUrls.get(0));
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
            allSecretsMapObj.put(REDIRECT_URL_KEY, redirectUrl);
        }
    }

    private void initScopes(List<String> scopes) {
        if (!CollectionUtils.isEmpty(scopes)) {
            final String SCOPES_KEY = "scopes";
            allSecretsMapObj.put(SCOPES_KEY, scopes);
        }
    }

    @Override
    public <T extends ClientConfiguration> AgentConfiguration<T> getAgentConfiguration(
            final Class<T> clientConfigClass) {

        // For local development we can use the development.yml file.
        if (!tppSecretsServiceEnabled) {
            return getAgentConfigurationDev(clientConfigClass);
        }

        Preconditions.checkNotNull(
                allSecretsMapObj,
                "Secrets were not fetched. Try to init() the AgentConfigurationController.");

        T clientConfig =
                Optional.ofNullable(OBJECT_MAPPER.convertValue(allSecretsMapObj, clientConfigClass))
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Agent configuration for agent: "
                                                        + clientConfigClass.toString()
                                                        + " is missing"
                                                        + getSecretsServiceParamsString()));

        String redirectUrl =
                Optional.ofNullable(
                                OBJECT_MAPPER.convertValue(
                                        allSecretsMapObj, AgentConfiguration.class))
                        .filter(config -> !config.isRedirectUrlNullOrEmpty())
                        .map(AgentConfiguration::getRedirectUrl)
                        // TPP Secrets is not used
                        .orElse(null);

        String qwac =
                Optional.ofNullable(
                                OBJECT_MAPPER.convertValue(
                                        allSecretsMapObj, AgentConfiguration.class))
                        .map(AgentConfiguration::getQwac)
                        .orElse("");

        String qsealc =
                Optional.ofNullable(
                                OBJECT_MAPPER.convertValue(
                                        allSecretsMapObj, AgentConfiguration.class))
                        .map(AgentConfiguration::getQsealc)
                        .orElse("");

        AgentConfiguration<T> agentConfiguration =
                new AgentConfiguration.Builder()
                        .setProviderSpecificConfiguration(clientConfig)
                        .setRedirectUrl(redirectUrl)
                        .setQwac(qwac)
                        .setQsealc(qsealc)
                        .build();

        return agentConfiguration;
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
                + ", providerId: "
                + providerId
                + ", appId: "
                + appId
                + " and clusterId: "
                + clusterId
                + " ";
    }

    @Override
    public <T extends ClientConfiguration> T getAgentConfigurationFromK8s(
            String integrationName, String clientName, Class<T> clientConfigClass) {

        if (isOpenBankingAgent) {
            log.warn(TRY_READ_FROM_K8_LOGGER_MSG, clientConfigClass);
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

    @Override
    public <T extends ClientConfiguration> T getAgentConfigurationFromK8s(
            String integrationName, Class<T> clientConfigClass) {
        if (isOpenBankingAgent) {
            log.warn(TRY_READ_FROM_K8_LOGGER_MSG, clientConfigClass);
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

    @Override
    public <T extends ClientConfiguration> Optional<T> getAgentConfigurationFromK8sAsOptional(
            String integrationName, Class<T> clientConfigClass) {
        if (isOpenBankingAgent) {
            log.warn(TRY_READ_FROM_K8_LOGGER_MSG, clientConfigClass);
        }

        Optional<Object> clientConfigurationAsObject =
                integrationsConfiguration.getIntegration(integrationName);

        clientConfigurationAsObject.ifPresent(this::extractSensitiveValues);

        return clientConfigurationAsObject.map(
                clientConfiguration ->
                        OBJECT_MAPPER.convertValue(clientConfiguration, clientConfigClass));
    }

    @Override
    public Set<String> extractSensitiveValues(Object clientConfigurationAsObject) {

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
    private <T extends ClientConfiguration> AgentConfiguration<T> getAgentConfigurationDev(
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
                                                        + ". In the development.yml and test.yml file."));

        extractSensitiveValues(clientConfigurationAsObject);

        AgentConfiguration<T> agentConfiguration =
                new AgentConfiguration.Builder()
                        .setProviderSpecificConfiguration(
                                OBJECT_MAPPER.convertValue(
                                        clientConfigurationAsObject, clientConfigClass))
                        .setRedirectUrl(
                                Optional.ofNullable(
                                                OBJECT_MAPPER.convertValue(
                                                        clientConfigurationAsObject,
                                                        AgentConfiguration.class))
                                        .filter(config -> !config.isRedirectUrlNullOrEmpty())
                                        .map(AgentConfiguration::getRedirectUrl)
                                        .orElse(null))
                        .setQwac(EIdasTinkCert.QWAC)
                        .setQsealc(EIdasTinkCert.QSEALC)
                        .build();

        return agentConfiguration;
    }
}
