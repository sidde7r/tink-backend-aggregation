package se.tink.backend.aggregation.resources;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import io.opentracing.Span;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.CollectionUtils;
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
import se.tink.backend.aggregation.resources.dispatcher.RefreshRequestDispatcher;
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
import se.tink.backend.aggregation.workers.abort.RequestAbortHandler;
import se.tink.backend.aggregation.workers.operation.AgentWorkerOperation;
import se.tink.backend.aggregation.workers.operation.RequestStatus;
import se.tink.backend.aggregation.workers.ratelimit.DefaultProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.OverridingProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.ratelimit.ProviderRateLimiterFactory;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.aggregation.workers.worker.AgentWorkerOperationFactory;
import se.tink.libraries.credentials.service.CreateCredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.HasRefreshScope;
import se.tink.libraries.credentials.service.ManualAuthenticateRequest;
import se.tink.libraries.credentials.service.RefreshInformationRequest;
import se.tink.libraries.credentials.service.RefreshScope;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.credentials.service.UpdateCredentialsRequest;
import se.tink.libraries.draining.ApplicationDrainMode;
import se.tink.libraries.http.utils.HttpResponseHelper;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.tracing.lib.api.Tracing;

@Path("/aggregation")
public class AggregationServiceResource implements AggregationService {

    private static final MetricId SERVICE_IMPLEMENTATION_LATENCY =
            MetricId.newId("aggregation_implementation_latency");
    private static final MetricId USER_AVAILABILITY =
            MetricId.newId("aggregation_user_availability");
    private static final MetricId REFRESH_SCOPE_PRESENCE =
            MetricId.newId("aggregation_refresh_scope_presence");
    private static final MetricId REFRESH_INCLUDED_IN_PAYMENT =
            MetricId.newId("aggregation_refresh_included_in_payment");
    private static final MetricId REFRESH_PRIORITY = MetricId.newId("aggregation_refresh_priority");

    private static final String CONFIGURE_WHITELIST = "configure_whitelist";
    private static final String REFRESH_WHITELIST = "refresh_whitelist";
    private static final String REFRESH = "refresh";
    private static final String APP_ID = "app_id";
    private static final String OPERATION_ID = "operation_id";
    private static final String CREDENTIALS_ID = "credentials_id";

    private final MetricRegistry metricRegistry;
    @Context private HttpServletRequest httpRequest;

    private final AgentWorker agentWorker;
    private final AgentWorkerOperationFactory agentWorkerCommandFactory;
    private final SupplementalInformationController supplementalInformationController;
    private final ApplicationDrainMode applicationDrainMode;
    private final ProviderConfigurationService providerConfigurationService;
    private final StartupChecksHandler startupChecksHandler;
    private final RequestAbortHandler requestAbortHandler;
    private final RefreshRequestDispatcher refreshRequestDispatcher;
    private static final Logger logger = LoggerFactory.getLogger(AggregationServiceResource.class);
    private static final List<? extends Number> BUCKETS =
            Arrays.asList(0., .005, .01, .025, .05, .1, .25, .5, 1., 2.5, 5., 10., 15, 35, 65, 110);

    @Inject
    public AggregationServiceResource(
            AgentWorker agentWorker,
            AgentWorkerOperationFactory agentWorkerOperationFactory,
            SupplementalInformationController supplementalInformationController,
            ApplicationDrainMode applicationDrainMode,
            ProviderConfigurationService providerConfigurationService,
            StartupChecksHandler startupChecksHandler,
            MetricRegistry metricRegistry,
            RequestAbortHandler requestAbortHandler,
            RefreshRequestDispatcher refreshRequestDispatcher) {
        this.agentWorker = agentWorker;
        this.agentWorkerCommandFactory = agentWorkerOperationFactory;
        this.supplementalInformationController = supplementalInformationController;
        this.applicationDrainMode = applicationDrainMode;
        this.providerConfigurationService = providerConfigurationService;
        this.startupChecksHandler = startupChecksHandler;
        this.metricRegistry = metricRegistry;
        this.requestAbortHandler = requestAbortHandler;
        this.refreshRequestDispatcher = refreshRequestDispatcher;
    }

    private void attachTracingInformation(CredentialsRequest request, ClientInfo clientInfo) {
        Span span = Tracing.getTracer().activeSpan();
        span.setTag(APP_ID, clientInfo.getAppId());
        span.setTag(
                OPERATION_ID, request.getOperationId() == null ? "N/A" : request.getOperationId());
        span.setTag(CREDENTIALS_ID, request.getCredentials().getId());
    }

    private void trackUserPresentFlagPresence(String method, CredentialsRequest request) {
        metricRegistry
                .meter(
                        USER_AVAILABILITY
                                .label("method", method)
                                .label("is_present", request.getUserAvailability() != null))
                .inc();
    }

    private void trackLatency(String endpoint, long durationMs) {
        metricRegistry
                .histogram(SERVICE_IMPLEMENTATION_LATENCY.label("endpoint", endpoint), BUCKETS)
                .update(durationMs / 1000.0);
    }

    @Override
    public Credentials createCredentials(CreateCredentialsRequest request, ClientInfo clientInfo) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            attachTracingInformation(request, clientInfo);

            AgentWorkerOperation createCredentialsOperation =
                    agentWorkerCommandFactory.createOperationCreateCredentials(request, clientInfo);

            createCredentialsOperation.run();

            return createCredentialsOperation.getRequest().getCredentials();
        } finally {
            trackLatency("create", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public String ping() {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            if (applicationDrainMode.isEnabled()) {
                HttpResponseHelper.error(Response.Status.SERVICE_UNAVAILABLE);
            }

            return "pong";
        } finally {
            trackLatency("ping", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public String started() {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            return startupChecksHandler.handle();
        } finally {
            trackLatency("started", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void configureWhitelistInformation(
            final ConfigureWhitelistInformationRequest request, ClientInfo clientInfo)
            throws Exception {

        Stopwatch sw = Stopwatch.createStarted();
        try {

            trackUserPresentFlagPresence(CONFIGURE_WHITELIST, request);
            trackRefreshScopePresence(CONFIGURE_WHITELIST, request);
            trackRefreshPriority(CONFIGURE_WHITELIST, request.getRefreshPriority());

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
                    agentWorkerCommandFactory.createOperationConfigureWhitelist(
                            request, clientInfo));
        } finally {
            trackLatency(CONFIGURE_WHITELIST, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void refreshWhitelistInformation(
            final RefreshWhitelistInformationRequest request, ClientInfo clientInfo)
            throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {

            trackUserPresentFlagPresence(REFRESH_WHITELIST, request);
            trackRefreshScopePresence(REFRESH_WHITELIST, request);
            trackRefreshPriority(REFRESH_WHITELIST, request.getRefreshPriority());

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
        } finally {
            trackLatency(REFRESH_WHITELIST, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void refreshInformation(final RefreshInformationRequest request, ClientInfo clientInfo)
            throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            attachTracingInformation(request, clientInfo);

            trackUserPresentFlagPresence(REFRESH, request);
            trackRefreshScopePresence(REFRESH, request);
            trackRefreshPriority(REFRESH, request.getRefreshPriority());

            refreshRequestDispatcher.dispatchRefreshInformation(request, clientInfo);
        } finally {
            trackLatency(REFRESH, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void authenticate(final ManualAuthenticateRequest request, ClientInfo clientInfo)
            throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            attachTracingInformation(request, clientInfo);

            trackUserPresentFlagPresence("authenticate", request);
            trackRefreshScopePresence("manual_authenticate", request);
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationAuthenticate(request, clientInfo));
        } finally {
            trackLatency("authenticate", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void transfer(final TransferRequest request, ClientInfo clientInfo) throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence("transfer", request);
            trackRefreshScopePresence("transfer", request);
            trackRefreshIncludedInPayment("transfer", !request.isSkipRefresh());
            logger.info(
                    "Transfer Request received from main. skipRefresh is: {}",
                    request.isSkipRefresh());
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationExecuteTransfer(request, clientInfo));

        } finally {
            trackLatency("transfer", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void payment(final TransferRequest request, ClientInfo clientInfo) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence("payment", request);
            trackRefreshScopePresence("payment", request);
            trackRefreshIncludedInPayment("payment", !request.isSkipRefresh());
            logger.info(
                    "Transfer Request received from main. skipRefresh is: {}",
                    request.isSkipRefresh());
            try {
                agentWorker.execute(
                        agentWorkerCommandFactory.createOperationExecutePayment(
                                request, clientInfo));
            } catch (Exception e) {
                logger.error("Error while calling createOperationExecutePayment", e);
            }
        } finally {
            trackLatency("payment", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void recurringPayment(RecurringPaymentRequest request, ClientInfo clientInfo) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence("recurring_payment", request);
            trackRefreshScopePresence("recurring_payment", request);
            trackRefreshIncludedInPayment("recurring_payment", !request.isSkipRefresh());
            logger.info("Recurring Payment Request received from main" + request);
            try {
                agentWorker.execute(
                        agentWorkerCommandFactory.createOperationExecutePayment(
                                request, clientInfo));
            } catch (Exception e) {
                logger.error("Error while calling createOperationRecurringPayment", e);
            }
        } finally {
            trackLatency("recurring_payment", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void whitelistedTransfer(final WhitelistedTransferRequest request, ClientInfo clientInfo)
            throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence("whitelisted_transfer", request);
            trackRefreshScopePresence("whitelisted_transfer", request);
            trackRefreshIncludedInPayment("whitelisted_transfer", !request.isSkipRefresh());
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationExecuteWhitelistedTransfer(
                            request, clientInfo));
        } finally {
            trackLatency("whitelisted_transfer", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public Credentials updateCredentials(UpdateCredentialsRequest request, ClientInfo clientInfo) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            attachTracingInformation(request, clientInfo);

            AgentWorkerOperation updateCredentialsOperation =
                    agentWorkerCommandFactory.createOperationUpdate(request, clientInfo);

            updateCredentialsOperation.run();

            return updateCredentialsOperation.getRequest().getCredentials();
        } finally {
            trackLatency("update", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private static ProviderRateLimiterFactory constructProviderRateLimiterFactoryFromRequest(
            ChangeProviderRateLimitsRequest request) {
        return new OverridingProviderRateLimiterFactory(
                request.getRatePerSecondByClassname(),
                new DefaultProviderRateLimiterFactory(request.getDefaultRate()));
    }

    @Override
    public void updateRateLimits(ChangeProviderRateLimitsRequest request) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            agentWorker
                    .getRateLimitedExecutorService()
                    .setRateLimiterFactory(constructProviderRateLimiterFactoryFromRequest(request));
        } finally {
            trackLatency("update_ratelimits", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void setSupplementalInformation(SupplementInformationRequest request) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            supplementalInformationController.setSupplementalInformation(
                    request.getCredentialsId(), request.getSupplementalInformation());
        } finally {
            trackLatency("set_supplemental", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public Response reEncryptCredentials(
            ReEncryptCredentialsRequest reencryptCredentialsRequest, ClientInfo clientInfo) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            try {
                agentWorker.execute(
                        agentWorkerCommandFactory.createOperationReEncryptCredentials(
                                reencryptCredentialsRequest, clientInfo));
            } catch (Exception e) {
                HttpResponseHelper.error(Response.Status.INTERNAL_SERVER_ERROR);
            }

            return HttpResponseHelper.ok();

        } finally {
            trackLatency("reencrypt", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public String getSecretsTemplate(
            String providerName,
            boolean includeDescriptions,
            boolean includeExamples,
            ClientInfo clientInfo) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            return new ClientConfigurationTemplateBuilder(
                            this.getProviderFromName(providerName, clientInfo),
                            includeDescriptions,
                            includeExamples)
                    .buildTemplate();
        } finally {
            trackLatency("get_secrets_template", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public String getSecretsJsonSchema(String providerName, ClientInfo clientInfo) {

        Stopwatch sw = Stopwatch.createStarted();
        try {
            return new ClientConfigurationJsonSchemaBuilder(
                            this.getProviderFromName(providerName, clientInfo))
                    .buildJsonSchema();
        } finally {
            trackLatency("get_secrets_schema", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public SecretsNamesValidationResponse validateSecretsNames(
            SecretsNamesValidationRequest request) {
        Stopwatch sw = Stopwatch.createStarted();
        try {

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
            Preconditions.checkState(
                    !allProviders.isEmpty(), "Should find at least 1 provider for all providers");

            List<ProviderConfiguration> filteredProviders =
                    allProviders.stream()
                            .filter(prv -> (Objects.equals(providerId, prv.getName())))
                            .filter(prv -> prv.getAccessType() == AccessType.OPEN_BANKING)
                            .collect(Collectors.toList());

            // return no provider found rather than 500 in this case
            if (filteredProviders.isEmpty() || filteredProviders.size() != 1) {
                return new SecretsNamesValidationResponse(
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        Collections.emptySet(),
                        providerId);
            }

            return new ClientConfigurationValidator(Provider.of(filteredProviders.get(0)))
                    .validate(
                            request.getSecretsNames(),
                            request.getExcludedSecretsNames(),
                            request.getSensitiveSecretsNames(),
                            request.getExcludedSensitiveSecretsNames(),
                            request.getAgentConfigParamNames(),
                            request.getExcludedAgentConfigParamNames());
        } finally {
            trackLatency("validate_secrets", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void createBeneficiary(
            CreateBeneficiaryCredentialsRequest request, ClientInfo clientInfo) throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {
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
        } finally {
            trackLatency("create_beneficiary", sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    private Provider getProviderFromName(String providerName, ClientInfo clientInfo) {
        Preconditions.checkNotNull(
                Strings.emptyToNull(providerName), "providerName cannot be empty/null.");

        String clusterName = "";
        String clusterEnvironment = "";
        if (!Objects.isNull(clientInfo)) {
            clusterName = clientInfo.getClusterName();
            clusterEnvironment = clientInfo.getClusterEnvironment();
            logger.info(
                    "clusterName {} clusterEnvironment {} when getting provider ",
                    clusterName,
                    clusterEnvironment);
        }

        ProviderConfiguration filteredProvider =
                providerConfigurationService.getProviderByNameInClusterIfPossible(
                        clusterName, clusterEnvironment, providerName);
        if (!Objects.isNull(filteredProvider)
                && filteredProvider.getAccessType() == AccessType.OPEN_BANKING
                && !StringUtils.containsIgnoreCase(filteredProvider.getName(), "sandbox")) {
            return Provider.of(filteredProvider);
        } else {
            HttpResponseHelper httpResponseHelper = new HttpResponseHelper(logger);
            httpResponseHelper.error(
                    Response.Status.NOT_FOUND,
                    String.format("Provider not found: %s", providerName));
            return null;
        }
    }

    private void trackRefreshScopePresence(String method, HasRefreshScope request) {
        trackRefreshScopePresence(method, request.getRefreshScope());
    }

    private void trackRefreshScopePresence(String method, RefreshScope refreshScope) {
        metricRegistry.meter(getRefreshScopeLabels(method, refreshScope)).inc();
    }

    private MetricId getRefreshScopeLabels(String method, RefreshScope refreshScope) {
        MetricId metricId =
                REFRESH_SCOPE_PRESENCE
                        .label("method", method)
                        .label("refresh_scope_present", refreshScope != null);
        if (refreshScope == null) {
            return metricId;
        }
        return metricId.label(
                        "refresh_scope_segments_present",
                        CollectionUtils.isNotEmpty(refreshScope.getFinancialServiceSegmentsIn()))
                .label(
                        "refresh_scope_refreshable_items_present",
                        CollectionUtils.isNotEmpty(refreshScope.getRefreshableItemsIn()))
                .label(
                        "refresh_scope_refreshable_items",
                        refreshScope.getRefreshableItemsIn() == null
                                ? 0
                                : refreshScope.getRefreshableItemsIn().size());
    }

    private void trackRefreshPriority(String method, Integer refreshPriority) {
        metricRegistry
                .meter(
                        REFRESH_PRIORITY
                                .label("method", method)
                                .label("refresh_priority_present", refreshPriority != null))
                .inc();
    }

    private void trackRefreshIncludedInPayment(String method, boolean refreshIncluded) {
        metricRegistry
                .meter(
                        REFRESH_INCLUDED_IN_PAYMENT
                                .label("method", method)
                                .label("refresh_included", refreshIncluded))
                .inc();
    }

    public Response getAbortRequestStatus(String credentialsId) {
        return requestAbortHandler(credentialsId);
    }

    public Response createAbortRequest(String credentialsId) {
        return requestAbortHandler(credentialsId);
    }

    private Response requestAbortHandler(String credentialsId) {
        Optional<RequestStatus> optionalStatus = requestAbortHandler.handle(credentialsId);

        if (!optionalStatus.isPresent()) {
            HttpResponseHelper httpResponseHelper = new HttpResponseHelper(logger);
            httpResponseHelper.error(
                    Response.Status.NOT_FOUND,
                    "Can not find any request for credentialsId: " + credentialsId);
            return null;
        }

        RequestStatus status = optionalStatus.get();

        // This is just because our current client (SDK) does not need this granularity
        if (status == RequestStatus.ABORTING || status == RequestStatus.IMPOSSIBLE_TO_ABORT) {
            status = RequestStatus.TRYING_TO_ABORT;
        }

        return Response.status(resolveResponseStatus(status))
                .entity(Collections.singletonMap("requestStatus", status.name()))
                .build();
    }

    private Response.Status resolveResponseStatus(RequestStatus status) {
        switch (status) {
            case TRYING_TO_ABORT:
            case ABORTING:
            case IMPOSSIBLE_TO_ABORT:
                return Response.Status.ACCEPTED;
            case ABORTING_OPERATION_SUCCEEDED:
            case ABORTING_OPERATION_FAILED:
                return Response.Status.OK;
            default:
                throw new IllegalStateException("Unexpected request status: " + status);
        }
    }
}
