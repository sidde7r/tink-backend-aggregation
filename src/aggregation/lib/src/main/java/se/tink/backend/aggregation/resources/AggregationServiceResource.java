package se.tink.backend.aggregation.resources;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.tools.ClientConfigurationJsonSchemaBuilder;
import se.tink.backend.aggregation.agents.tools.ClientConfigurationTemplateBuilder;
import se.tink.backend.aggregation.agents.tools.validator.ClientConfigurationValidator;
import se.tink.backend.aggregation.api.AggregationService;
import se.tink.backend.aggregation.api.WhitelistedTransferRequest;
import se.tink.backend.aggregation.client.provider_configuration.ProviderConfigurationService;
import se.tink.backend.aggregation.client.provider_configuration.rpc.ProviderConfiguration;
import se.tink.backend.aggregation.client.provider_configuration.rpc.ProviderConfiguration.AccessType;
import se.tink.backend.aggregation.cluster.identification.ClientInfo;
import se.tink.backend.aggregation.controllers.SupplementalInformationController;
import se.tink.backend.aggregation.queue.models.RefreshInformation;
import se.tink.backend.aggregation.rpc.ChangeProviderRateLimitsRequest;
import se.tink.backend.aggregation.rpc.ConfigureWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.CreateBeneficiaryCredentialsRequest;
import se.tink.backend.aggregation.rpc.ReEncryptCredentialsRequest;
import se.tink.backend.aggregation.rpc.RecurringPaymentRequest;
import se.tink.backend.aggregation.rpc.RefreshWhitelistInformationRequest;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationRequest;
import se.tink.backend.aggregation.rpc.SecretsNamesValidationResponse;
import se.tink.backend.aggregation.rpc.SupplementInformationRequest;
import se.tink.backend.aggregation.rpc.TransferRequest;
import se.tink.backend.aggregation.startupchecks.StartupChecksHandler;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.ratelimit.DefaultProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.OverridingProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.ProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.backend.aggregation.workers.worker.AgentWorkerRefreshOperationCreatorWrapper;
import se.tink.libraries.credentials.service.CreateCredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UpdateCredentialsRequest;
import se.tink.libraries.draining.ApplicationDrainMode;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.queue.QueueProducer;

@Path("/aggregation")
public class AggregationServiceResource implements AggregationService {

    private static final MetricId USER_AVAILABILITY =
            MetricId.newId("aggregation_user_availability");
    private static final MetricId USER_AVAILABILITY_VALUES =
            MetricId.newId("aggregation_user_availability_values");

    private final MetricRegistry metricRegistry;
    private final QueueProducer producer;
    @Context private HttpServletRequest httpRequest;

    private AgentWorker agentWorker;
    private AgentWorkerOperationFactory agentWorkerCommandFactory;
    private SupplementalInformationController supplementalInformationController;
    private ApplicationDrainMode applicationDrainMode;
    private ProviderConfigurationService providerConfigurationService;
    private StartupChecksHandler startupChecksHandler;
    private static final Logger logger = LoggerFactory.getLogger(AggregationServiceResource.class);

    @Inject
    public AggregationServiceResource(
            AgentWorker agentWorker,
            QueueProducer producer,
            AgentWorkerOperationFactory agentWorkerOperationFactory,
            SupplementalInformationController supplementalInformationController,
            ApplicationDrainMode applicationDrainMode,
            ProviderConfigurationService providerConfigurationService,
            StartupChecksHandler startupChecksHandler,
            MetricRegistry metricRegistry) {
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerOperationFactory;
        this.supplementalInformationController = supplementalInformationController;
        this.producer = producer;
        this.applicationDrainMode = applicationDrainMode;
        this.providerConfigurationService = providerConfigurationService;
        this.startupChecksHandler = startupChecksHandler;
        this.metricRegistry = metricRegistry;
    }

    private void trackUserPresentFlagPresence(String method, CredentialsRequest request) {
        metricRegistry
                .meter(
                        USER_AVAILABILITY
                                .label("method", method)
                                .label("is_present", request.getUserAvailability() != null))
                .inc();
    }

    private boolean isHighPrioRequest(CredentialsRequest request) {
        // The UserAvailability object and data that follows is the new (2021-03-15) way of taking
        // decisions on Aggregation Service in relation to the User. For instance, we want to
        // high-prioritize all requests where the user is present.
        if (request.getUserAvailability() != null) {
            metricRegistry
                    .meter(
                            USER_AVAILABILITY_VALUES
                                    .label("manual", request.isManual())
                                    .label("present", request.getUserAvailability().isUserPresent())
                                    .label(
                                            "available_for_interaction",
                                            request.getUserAvailability()
                                                    .isUserAvailableForInteraction()))
                    .inc();

            return request.getUserAvailability().isUserPresent();
        }

        // fallback to old flag
        return request.isManual();
    }

    @Override
    public Credentials createCredentials(CreateCredentialsRequest request, ClientInfo clientInfo) {
        AgentWorkerOperation createCredentialsOperation =
                agentWorkerCommandFactory.createOperationCreateCredentials(request, clientInfo);

        createCredentialsOperation.run();

        return createCredentialsOperation.getRequest().getCredentials();
    }

    @Override
    public String ping() {
        if (applicationDrainMode.isEnabled()) {
            HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
        }

        return "pong";
    }

    @Override
    public String started() {
        return startupChecksHandler.handle();
    }

    @Override
    public void configureWhitelistInformation(
            final ConfigureWhitelistInformationRequest request, ClientInfo clientInfo)
            throws Exception {

        trackUserPresentFlagPresence("configure_whitelist", request);

        Set<RefreshableItem> itemsToRefresh = request.getItemsToRefresh();

        // If the caller don't set any refreshable items, we won't do a refresh
        if (Objects.isNull(itemsToRefresh) || itemsToRefresh.isEmpty()) {
            logger.warn(
                    "Provided Refreshable items are empty for credentialsId: {}",
                    request.getCredentials().getId());
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        // If the caller don't set any account type refreshable item, we don't do a refresh
        if (!RefreshableItem.hasAccounts(itemsToRefresh)) {
            logger.warn(
                    "No accounts to refresh for credentialsId: {} because of refreshableItems provided: {}",
                    request.getCredentials().getId(),
                    itemsToRefresh);
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }
        agentWorker.execute(
                agentWorkerCommandFactory.createOperationConfigureWhitelist(request, clientInfo));
    }

    @Override
    public void refreshWhitelistInformation(
            final RefreshWhitelistInformationRequest request, ClientInfo clientInfo)
            throws Exception {

        trackUserPresentFlagPresence("refresh_whitelist", request);

        // If the caller don't set any accounts to refresh, we won't do a refresh.
        if (Objects.isNull(request.getAccounts()) || request.getAccounts().isEmpty()) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        Set<RefreshableItem> itemsToRefresh = request.getItemsToRefresh();

        // If the caller don't sets any refreshable items, we won't do a refresh
        if (Objects.isNull(itemsToRefresh) || itemsToRefresh.isEmpty()) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }

        // If the caller don't sets any account type refreshable item, we don't do a refresh
        if (!RefreshableItem.hasAccounts(itemsToRefresh)) {
            HttpResponseHelper.error(Response.Status.BAD_REQUEST);
        }
        agentWorker.execute(
                agentWorkerCommandFactory.createOperationWhitelistRefresh(request, clientInfo));
    }

    @Override
    public void refreshInformation(final RefreshInformationRequest request, ClientInfo clientInfo)
            throws Exception {
        trackUserPresentFlagPresence("refresh", request);

        if (isHighPrioRequest(request)) {
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationRefresh(request, clientInfo));
        } else {
            if (producer.isAvailable()) {
                producer.send(new RefreshInformation(request, clientInfo));
            } else {
                agentWorker.executeAutomaticRefresh(
                        AgentWorkerRefreshOperationCreatorWrapper.of(
                                agentWorkerCommandFactory, request, clientInfo));
            }
        }
    }

    @Override
    public void authenticate(final ManualAuthenticateRequest request, ClientInfo clientInfo)
            throws Exception {
        trackUserPresentFlagPresence("authenticate", request);
        agentWorker.execute(
                agentWorkerCommandFactory.createOperationAuthenticate(request, clientInfo));
    }

    @Override
    public void transfer(final TransferRequest request, ClientInfo clientInfo) throws Exception {
        trackUserPresentFlagPresence("transfer", request);
        logger.info(
                "Transfer Request received from main. skipRefresh is: {}", request.isSkipRefresh());
        agentWorker.execute(
                agentWorkerCommandFactory.createOperationExecuteTransfer(request, clientInfo));
    }

    @Override
    public void payment(final TransferRequest request, ClientInfo clientInfo) {
        trackUserPresentFlagPresence("payment", request);
        logger.info(
                "Transfer Request received from main. skipRefresh is: {}", request.isSkipRefresh());
        try {
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationExecutePayment(request, clientInfo));
        } catch (Exception e) {
            logger.error("Error while calling createOperationExecutePayment", e);
        }
    }

    @Override
    public void recurringPayment(RecurringPaymentRequest request, ClientInfo clientInfo) {
        trackUserPresentFlagPresence("recurring_payment", request);
        logger.info("Recurring Payment Request received from main");
        try {
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationExecutePayment(request, clientInfo));
        } catch (Exception e) {
            logger.error("Error while calling createOperationRecurringPayment", e);
        }
    }

    @Override
    public void whitelistedTransfer(final WhitelistedTransferRequest request, ClientInfo clientInfo)
            throws Exception {
        trackUserPresentFlagPresence("whitelisted_transfer", request);
        agentWorker.execute(
                agentWorkerCommandFactory.createOperationExecuteWhitelistedTransfer(
                        request, clientInfo));
    }

    @Override
    public Credentials updateCredentials(UpdateCredentialsRequest request, ClientInfo clientInfo) {
        AgentWorkerOperation updateCredentialsOperation =
                agentWorkerCommandFactory.createOperationUpdate(request, clientInfo);

        updateCredentialsOperation.run();

        return updateCredentialsOperation.getRequest().getCredentials();
    }

    private static ProviderRateLimiterFactory constructProviderRateLimiterFactoryFromRequest(
            ChangeProviderRateLimitsRequest request) {
        return new OverridingProviderRateLimiterFactory(
                request.getRatePerSecondByClassname(),
                new DefaultProviderRateLimiterFactory(request.getDefaultRate()));
    }

    @Override
    public void updateRateLimits(ChangeProviderRateLimitsRequest request) {
        agentWorker
                .getRateLimitedExecutorService()
                .setRateLimiterFactory(constructProviderRateLimiterFactoryFromRequest(request));
    }

    @Override
    public void setSupplementalInformation(SupplementInformationRequest request) {
        supplementalInformationController.setSupplementalInformation(
                request.getCredentialsId(), request.getSupplementalInformation());
    }

    @Override
    public Response reEncryptCredentials(
            ReEncryptCredentialsRequest reencryptCredentialsRequest, ClientInfo clientInfo) {
        try {
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationReEncryptCredentials(
                            reencryptCredentialsRequest, clientInfo));
        } catch (Exception e) {
            HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
        }

        return HttpResponseHelper.ok();
    }

    @Override
    public String getSecretsTemplate(
            String providerName,
            boolean includeDescriptions,
            boolean includeExamples,
            ClientInfo clientInfo) {
        return new ClientConfigurationTemplateBuilder(
                        this.getProviderFromName(providerName),
                        includeDescriptions,
                        includeExamples)
                .buildTemplate();
    }

    @Override
    public String getSecretsJsonSchema(String providerName, ClientInfo clientInfo) {

        return new ClientConfigurationJsonSchemaBuilder(this.getProviderFromName(providerName))
                .buildJsonSchema();
    }

    @Override
    public SecretsNamesValidationResponse validateSecretsNames(
            SecretsNamesValidationRequest request) {
        Preconditions.checkNotNull(request, "SecretsNamesValidationRequest cannot be null.");

        String providerId = request.getProviderId();

        Preconditions.checkArgument(
                !Strings.isNullOrEmpty(providerId), "The request must contain providerId.");
        Preconditions.checkNotNull(
                request.getSecretsNames(),
                "SecretsNames in SecretsNamesValidationRequest cannot be null.");
        Preconditions.checkNotNull(
                request.getExcludedSecretsNames(),
                "ExcludedSecretsNames in SecretsNamesValidationRequest cannot be null.");
        Preconditions.checkNotNull(
                request.getSensitiveSecretsNames(),
                "SensitiveSecretsNames in SecretsNamesValidationRequest cannot be null.");
        Preconditions.checkNotNull(
                request.getExcludedSensitiveSecretsNames(),
                "ExcludedSensitiveSecretsNames in SecretsNamesValidationRequest cannot be null.");
        Preconditions.checkNotNull(
                request.getAgentConfigParamNames(),
                "AgentConfigParamNames in SecretsNamesValidationRequest cannot be null.");
        Preconditions.checkNotNull(
                request.getExcludedAgentConfigParamNames(),
                "ExcludedAgentConfigParamNames in SecretsNamesValidationRequest cannot be null.");

        List<ProviderConfiguration> allProviders = providerConfigurationService.listAll();

        List<ProviderConfiguration> filteredProviders =
                allProviders.stream()
                        .filter(prv -> (Objects.equals(providerId, prv.getName())))
                        .filter(prv -> prv.getAccessType() == AccessType.OPEN_BANKING)
                        .collect(Collectors.toList());

        Preconditions.checkState(
                !allProviders.isEmpty(), "Should find at least 1 provider for all providers");

        Preconditions.checkState(
                !filteredProviders.isEmpty() && filteredProviders.size() == 1,
                "Should find 1 provider for the request providerId");

        return new ClientConfigurationValidator(Provider.of(filteredProviders.get(0)))
                .validate(
                        request.getSecretsNames(),
                        request.getExcludedSecretsNames(),
                        request.getSensitiveSecretsNames(),
                        request.getExcludedSensitiveSecretsNames(),
                        request.getAgentConfigParamNames(),
                        request.getExcludedAgentConfigParamNames(),
                        // will remove nonUniqueProviderNames after the removal of its usage in ESS
                        "");
    }

    @Override
    public void createBeneficiary(
            CreateBeneficiaryCredentialsRequest request, ClientInfo clientInfo) throws Exception {
        trackUserPresentFlagPresence("create_beneficiary", request);
        logger.info("Received create beneficiary request");
        // Only execute if feature is enabled with feature flag.
        Optional<AgentWorkerOperation> workerCommand =
                agentWorkerCommandFactory.createOperationCreateBeneficiary(request, clientInfo);
        if (workerCommand.isPresent()) {
            agentWorker.execute(workerCommand.get());
        } else {
            logger.warn("Feature is not enabled/implemented.");
        }
    }

    private Provider getProviderFromName(String providerName) {
        Preconditions.checkNotNull(
                Strings.emptyToNull(providerName), "providerName cannot be empty/null.");

        List<ProviderConfiguration> allProviders = providerConfigurationService.listAll();

        List<ProviderConfiguration> filteredProviders =
                allProviders.stream()
                        .filter(prv -> Objects.equals(providerName, prv.getName()))
                        .filter(prv -> prv.getAccessType() == AccessType.OPEN_BANKING)
                        // Trying to get rid of possible sandbox providers if they exist.
                        .filter(prv -> !StringUtils.containsIgnoreCase(prv.getName(), "sandbox"))
                        .collect(Collectors.toList());

        HttpResponseHelper httpResponseHelper = new HttpResponseHelper(logger);

        if (filteredProviders.size() == 0) {
            httpResponseHelper.error(
                    Response.Status.NOT_FOUND,
                    String.format("Provider not found: %s", providerName));
        } else if (filteredProviders.size() != 1) {
            httpResponseHelper.error(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    String.format(
                            "Trying to get secrets template. Should find 1 provider for providerName : %s, but found instead : %d",
                            providerName, filteredProviders.size()));
        }

        return Provider.of(filteredProviders.get(0));
    }
}
