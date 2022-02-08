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
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.FinancialService;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.agents.rpc.ProviderStatuses;
import se.tink.backend.agents.rpc.SelectOption;
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
import se.tink.libraries.provider.ProviderDto;
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
    private static final MetricId PAYMENT_REQUEST_ID_INCLUDED_IN_TRANSFER =
            MetricId.newId("aggregation_payment_request_id_included_in_transfer");
    private static final MetricId REFRESH_PRIORITY = MetricId.newId("aggregation_refresh_priority");

    private static final String APP_ID = "app_id";
    private static final String OPERATION_ID = "operation_id";
    private static final String CREDENTIALS_ID = "credentials_id";

    private static final String CREATE = "create";
    private static final String UPDATE = "update";
    private static final String PING = "ping";
    private static final String STARTED = "started";
    private static final String CONFIGURE_WHITELIST = "configure_whitelist";
    private static final String REFRESH_WHITELIST = "refresh_whitelist";
    private static final String REFRESH = "refresh";
    private static final String AUTHENTICATE = "authenticate";
    private static final String RECURRING_PAYMENT = "recurring_payment";
    private static final String WHITELISTED_TRANSFER = "whitelisted_transfer";
    private static final String UPDATE_RATELIMITS = "update_ratelimits";
    private static final String SET_SUPPLEMENTAL = "set_supplemental";
    private static final String REENCRYPT = "reencrypt";
    private static final String GET_SECRETS_TEMPLATE = "get_secrets_template";
    private static final String GET_SECRETS_SCHEMA = "get_secrets_schema";
    private static final String VALIDATE_SECRETS = "validate_secrets";
    private static final String CREATE_BENEFICIARY = "create_beneficiary";
    private static final String TRANSFER = "transfer";
    private static final String PAYMENT = "payment";

    private static final String ENDPOINT = "endpoint";
    private static final String METHOD = "method";
    private static final String IS_PRESENT = "is_present";
    private static final String REFRESH_INCLUDED = "refresh_included";
    private static final String REFRESH_PRIORITY_PRESENT = "refresh_priority_present";

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
        initMetrics();
    }

    private void initMetrics() {
        final String[] allEndpoints = {
            CREATE,
            UPDATE,
            PING,
            STARTED,
            CONFIGURE_WHITELIST,
            AUTHENTICATE,
            REFRESH,
            REFRESH_WHITELIST,
            UPDATE_RATELIMITS,
            SET_SUPPLEMENTAL,
            REENCRYPT,
            GET_SECRETS_TEMPLATE,
            GET_SECRETS_SCHEMA,
            VALIDATE_SECRETS,
            CREATE_BENEFICIARY,
            TRANSFER,
            PAYMENT,
            RECURRING_PAYMENT,
            WHITELISTED_TRANSFER,
        };
        final List<String> payments =
                Arrays.asList(TRANSFER, RECURRING_PAYMENT, WHITELISTED_TRANSFER, PAYMENT);
        final List<String> priorityIncluded =
                Arrays.asList(CONFIGURE_WHITELIST, REFRESH, REFRESH_WHITELIST);
        for (String endpoint : allEndpoints) {
            metricRegistry.histogram(SERVICE_IMPLEMENTATION_LATENCY.label(ENDPOINT, endpoint));
            metricRegistry.meter(
                    USER_AVAILABILITY.label(METHOD, endpoint).label(IS_PRESENT, false));
            metricRegistry.meter(USER_AVAILABILITY.label(METHOD, endpoint).label(IS_PRESENT, true));
            if (payments.contains(endpoint)) {
                metricRegistry.meter(
                        REFRESH_INCLUDED_IN_PAYMENT
                                .label(METHOD, endpoint)
                                .label(REFRESH_INCLUDED, false));
                metricRegistry.meter(
                        REFRESH_INCLUDED_IN_PAYMENT
                                .label(METHOD, endpoint)
                                .label(REFRESH_INCLUDED, true));
            }
            if (priorityIncluded.contains(endpoint)) {
                metricRegistry.meter(
                        REFRESH_PRIORITY
                                .label(METHOD, endpoint)
                                .label(REFRESH_PRIORITY_PRESENT, false));
                metricRegistry.meter(
                        REFRESH_PRIORITY
                                .label(METHOD, endpoint)
                                .label(REFRESH_PRIORITY_PRESENT, true));
            }
        }
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
                                .label(METHOD, method)
                                .label(IS_PRESENT, request.getUserAvailability() != null))
                .inc();
    }

    private void trackLatency(String endpoint, long durationMs) {
        metricRegistry
                .histogram(SERVICE_IMPLEMENTATION_LATENCY.label(ENDPOINT, endpoint), BUCKETS)
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
            trackLatency(CREATE, sw.stop().elapsed(TimeUnit.MILLISECONDS));
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
            trackLatency(PING, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public String started() {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            return startupChecksHandler.handle();
        } finally {
            trackLatency(STARTED, sw.stop().elapsed(TimeUnit.MILLISECONDS));
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
            try {
                agentWorker.execute(
                        agentWorkerCommandFactory.createOperationConfigureWhitelist(
                                request, clientInfo));
            } catch (Exception e) {
                logger.error("Error while calling createOperationConfigureWhitelist", e);
            }

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
                logger.info("RefreshWhitelistInformation - accounts empty");
                HttpResponseHelper.error(Response.Status.BAD_REQUEST);
            }

            Set<RefreshableItem> itemsToRefresh = request.getItemsToRefresh();

            // If the caller don't sets any refreshable items, we won't do a refresh
            if (Objects.isNull(itemsToRefresh) || itemsToRefresh.isEmpty()) {
                logger.info("RefreshWhitelistInformation - no items to refresh");
                HttpResponseHelper.error(Response.Status.BAD_REQUEST);
            }

            // If the caller don't sets any account type refreshable item, we don't do a refresh
            if (!RefreshableItem.hasAccounts(itemsToRefresh)) {
                logger.info("RefreshWhitelistInformation - no accounts in item to refresh");
                HttpResponseHelper.error(Response.Status.BAD_REQUEST);
            }
            try {
                agentWorker.execute(
                        agentWorkerCommandFactory.createOperationWhitelistRefresh(
                                request, clientInfo));
            } catch (Exception e) {
                logger.error("Error while calling createOperationWhitelistRefresh", e);
            }
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
            try {
                refreshRequestDispatcher.dispatchRefreshInformation(request, clientInfo);
            } catch (Exception e) {
                logger.error("Error while calling dispatchRefreshInformation", e);
            }
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

            trackUserPresentFlagPresence(AUTHENTICATE, request);
            trackRefreshScopePresence("manual_authenticate", request);
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationAuthenticate(request, clientInfo));
        } finally {
            trackLatency(AUTHENTICATE, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void transfer(final TransferRequest request, ClientInfo clientInfo) throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence(TRANSFER, request);
            trackRefreshScopePresence(TRANSFER, request);
            trackRefreshIncludedInPayment(TRANSFER, !request.isSkipRefresh());
            trackPaymentRequestIdPresence(TRANSFER, request.getPaymentRequestId());
            logger.info(
                    "Transfer Request received from main. skipRefresh is: {}",
                    request.isSkipRefresh());
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationExecuteTransfer(request, clientInfo));

        } finally {
            trackLatency(TRANSFER, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void payment(final TransferRequest request, ClientInfo clientInfo) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence(PAYMENT, request);
            trackRefreshScopePresence(PAYMENT, request);
            trackRefreshIncludedInPayment(PAYMENT, !request.isSkipRefresh());
            trackPaymentRequestIdPresence(PAYMENT, request.getPaymentRequestId());
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
            trackLatency(PAYMENT, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void recurringPayment(RecurringPaymentRequest request, ClientInfo clientInfo) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence(RECURRING_PAYMENT, request);
            trackRefreshScopePresence(RECURRING_PAYMENT, request);
            trackRefreshIncludedInPayment(RECURRING_PAYMENT, !request.isSkipRefresh());
            logger.info("Recurring Payment Request received from main" + request);
            try {
                agentWorker.execute(
                        agentWorkerCommandFactory.createOperationExecutePayment(
                                request, clientInfo));
            } catch (Exception e) {
                logger.error("Error while calling createOperationRecurringPayment", e);
            }
        } finally {
            trackLatency(RECURRING_PAYMENT, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void whitelistedTransfer(final WhitelistedTransferRequest request, ClientInfo clientInfo)
            throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence(WHITELISTED_TRANSFER, request);
            trackRefreshScopePresence(WHITELISTED_TRANSFER, request);
            trackRefreshIncludedInPayment(WHITELISTED_TRANSFER, !request.isSkipRefresh());
            agentWorker.execute(
                    agentWorkerCommandFactory.createOperationExecuteWhitelistedTransfer(
                            request, clientInfo));
        } finally {
            trackLatency(WHITELISTED_TRANSFER, sw.stop().elapsed(TimeUnit.MILLISECONDS));
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
            trackLatency(UPDATE, sw.stop().elapsed(TimeUnit.MILLISECONDS));
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
            trackLatency(UPDATE_RATELIMITS, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void setSupplementalInformation(SupplementInformationRequest request) {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            supplementalInformationController.setSupplementalInformation(
                    request.getCredentialsId(), request.getSupplementalInformation());
        } finally {
            trackLatency(SET_SUPPLEMENTAL, sw.stop().elapsed(TimeUnit.MILLISECONDS));
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
            trackLatency(REENCRYPT, sw.stop().elapsed(TimeUnit.MILLISECONDS));
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
            trackLatency(GET_SECRETS_TEMPLATE, sw.stop().elapsed(TimeUnit.MILLISECONDS));
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
            trackLatency(GET_SECRETS_SCHEMA, sw.stop().elapsed(TimeUnit.MILLISECONDS));
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

            Provider provider = convertProvider(filteredProviders.get(0));
            return new ClientConfigurationValidator(provider)
                    .validate(
                            request.getSecretsNames(),
                            request.getExcludedSecretsNames(),
                            request.getSensitiveSecretsNames(),
                            request.getExcludedSensitiveSecretsNames(),
                            request.getAgentConfigParamNames(),
                            request.getExcludedAgentConfigParamNames());
        } finally {
            trackLatency(VALIDATE_SECRETS, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    @Override
    public void createBeneficiary(
            CreateBeneficiaryCredentialsRequest request, ClientInfo clientInfo) throws Exception {
        Stopwatch sw = Stopwatch.createStarted();
        try {
            trackUserPresentFlagPresence(CREATE_BENEFICIARY, request);
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
            trackLatency(CREATE_BENEFICIARY, sw.stop().elapsed(TimeUnit.MILLISECONDS));
        }
    }

    // The API will be discussed again to revalidate its design
    @Override
    public Response getAbortRequestStatus(String credentialsId) {
        return requestAbortHandler(credentialsId);
    }

    @Override
    public Response createAbortRequest(String credentialsId) {
        return requestAbortHandler(credentialsId);
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
            return convertProvider(filteredProvider);
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
                        .label(METHOD, method)
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

    private void trackPaymentRequestIdPresence(String method, String paymentRequestId) {
        try {
            MetricId metricId =
                    PAYMENT_REQUEST_ID_INCLUDED_IN_TRANSFER
                            .label(METHOD, method)
                            .label("payment_request_id_present", paymentRequestId != null);
            metricRegistry.meter(metricId).inc();
        } catch (Exception e) {
            logger.error("Problems sending metric payment_request_id_present", e);
        }
    }

    private void trackRefreshPriority(String method, Integer refreshPriority) {
        metricRegistry
                .meter(
                        REFRESH_PRIORITY
                                .label(METHOD, method)
                                .label(REFRESH_PRIORITY_PRESENT, refreshPriority != null))
                .inc();
    }

    private void trackRefreshIncludedInPayment(String method, boolean refreshIncluded) {
        metricRegistry
                .meter(
                        REFRESH_INCLUDED_IN_PAYMENT
                                .label(METHOD, method)
                                .label(REFRESH_INCLUDED, refreshIncluded))
                .inc();
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
            case OPERATION_COMPLETED_WITHOUT_ABORT:
                return Response.Status.OK;
            default:
                throw new IllegalStateException("Unexpected request status: " + status);
        }
    }

    private static Provider convertProvider(ProviderConfiguration providerConfiguration) {
        Provider provider = new Provider();

        provider.setAccessType(
                Provider.AccessType.valueOf(providerConfiguration.getAccessType().name()));
        if (providerConfiguration.getAuthenticationFlow() != null) {
            provider.setAuthenticationFlow(
                    Provider.AuthenticationFlow.valueOf(
                            providerConfiguration.getAuthenticationFlow().name()));
        }
        provider.setClassName(providerConfiguration.getClassName());
        provider.setCredentialsType(
                CredentialsTypes.valueOf(providerConfiguration.getCredentialsType().name()));
        provider.setCurrency(providerConfiguration.getCurrency());
        provider.setDisplayName(providerConfiguration.getDisplayName());
        provider.setFields(
                providerConfiguration.getFields().stream()
                        .map(AggregationServiceResource::convertField)
                        .collect(Collectors.toList()));
        provider.setFinancialInstitutionId(providerConfiguration.getFinancialInstitutionId());
        provider.setMarket(providerConfiguration.getMarket());
        provider.setName(providerConfiguration.getName());
        provider.setPayload(providerConfiguration.getPayload());
        provider.setStatus(ProviderStatuses.valueOf(providerConfiguration.getStatus().name()));
        provider.setSupplementalFields(
                providerConfiguration.getSupplementalFields().stream()
                        .map(AggregationServiceResource::convertField)
                        .collect(Collectors.toList()));
        provider.setType(ProviderDto.ProviderTypes.valueOf(providerConfiguration.getType().name()));
        provider.setAuthenticationUserType(
                Provider.AuthenticationUserType.valueOf(
                        providerConfiguration.getAuthenticationUserType().name()));
        provider.setFinancialServices(
                CollectionUtils.emptyIfNull(providerConfiguration.getFinancialServices()).stream()
                        .map(AggregationServiceResource::convertFinancialService)
                        .collect(Collectors.toList()));
        return provider;
    }

    public static FinancialService convertFinancialService(
            se.tink.backend.aggregation.client.provider_configuration.rpc.FinancialService
                    financialService) {
        return new FinancialService()
                .setSegment(
                        FinancialService.FinancialServiceSegment.valueOf(
                                financialService.getSegment().name()))
                .setShortName(financialService.getShortName());
    }

    private static Field convertField(
            se.tink.backend.aggregation.client.provider_configuration.rpc.Field field) {
        return Field.builder()
                .additionalInfo(field.getAdditionalInfo())
                .checkbox(field.isCheckbox())
                .description(field.getDescription())
                .helpText(field.getHelpText())
                .hint(field.getHint())
                .immutable(field.isImmutable())
                .masked(field.isMasked())
                .maxLength(field.getMaxLength())
                .minLength(field.getMinLength())
                .name(field.getName())
                .numeric(field.isNumeric())
                .optional(field.isOptional())
                .pattern(field.getPattern())
                .patternError(field.getPatternError())
                .value(field.getValue())
                .selectOptions(
                        field.getSelectOptions() != null
                                ? field.getSelectOptions().stream()
                                        .map(
                                                o ->
                                                        new SelectOption(
                                                                o.getText(),
                                                                o.getValue(),
                                                                o.getIconUrl()))
                                        .collect(Collectors.toList())
                                : null)
                .build();
    }
}
