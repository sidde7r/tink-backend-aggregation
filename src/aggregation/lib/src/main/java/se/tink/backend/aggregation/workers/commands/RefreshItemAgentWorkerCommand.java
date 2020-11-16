package se.tink.backend.aggregation.workers.commands;

import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.ACCESS_EXCEEDED;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.BANK_SIDE_FAILURE;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.CONSENT_EXPIRED;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.CONSENT_INVALID;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.CONSENT_REVOKED;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.CONSENT_REVOKED_BY_USER;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.MULTIPLE_LOGIN;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.NO_BANK_SERVICE;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.SESSION_TERMINATED;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshBeneficiariesExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.compliance.customer_restrictions.CustomerDataFetchingRestrictions;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.events.RefreshEventProducer;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.client.serialization.IdentityDataSerializer;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent.AdditionalInfo;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.pair.Pair;

public class RefreshItemAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final Logger log = LoggerFactory.getLogger(RefreshItemAgentWorkerCommand.class);

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "refresh";

    private final AgentWorkerCommandContext context;
    private final RefreshableItem item;
    private final AgentWorkerCommandMetricState metrics;
    private final AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient;
    private final DataTrackerEventProducer dataTrackerEventProducer;
    private final RefreshEventProducer refreshEventProducer;
    private final List<DataFetchingRestrictions> dataFetchingRestrictions;
    private final CustomerDataFetchingRestrictions customerDataFetchingRestrictions =
            new CustomerDataFetchingRestrictions();

    private final String agentName;
    private final String provider;
    private final String market;
    private static final ImmutableMap<BankServiceError, AdditionalInfo>
            ADDITIONAL_INFO_ERROR_MAPPER =
                    ImmutableMap.<BankServiceError, AdditionalInfo>builder()
                            .put(ACCESS_EXCEEDED, AdditionalInfo.ACCESS_EXCEEDED)
                            .put(BANK_SIDE_FAILURE, AdditionalInfo.BANK_SIDE_FAILURE)
                            .put(CONSENT_EXPIRED, AdditionalInfo.CONSENT_EXPIRED)
                            .put(CONSENT_INVALID, AdditionalInfo.CONSENT_INVALID)
                            .put(CONSENT_REVOKED_BY_USER, AdditionalInfo.CONSENT_REVOKED)
                            .put(CONSENT_REVOKED, AdditionalInfo.CONSENT_REVOKED)
                            .put(MULTIPLE_LOGIN, AdditionalInfo.MULTIPLE_LOGIN)
                            .put(NO_BANK_SERVICE, AdditionalInfo.NO_BANK_SERVICE)
                            .put(SESSION_TERMINATED, AdditionalInfo.SESSION_TERMINATED)
                            .build();

    public RefreshItemAgentWorkerCommand(
            AgentWorkerCommandContext context,
            RefreshableItem item,
            AgentWorkerCommandMetricState metrics,
            AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient,
            DataTrackerEventProducer dataTrackerEventProducer,
            RefreshEventProducer refreshEventProducer) {
        this.context = context;
        this.item = item;
        this.agentDataAvailabilityTrackerClient = agentDataAvailabilityTrackerClient;
        this.dataTrackerEventProducer = dataTrackerEventProducer;
        this.refreshEventProducer = refreshEventProducer;
        CredentialsRequest request = context.getRequest();
        this.agentName = request.getProvider().getClassName();
        this.provider = request.getProvider().getName();
        this.market = request.getProvider().getMarket();
        this.dataFetchingRestrictions = request.getDataFetchingRestrictions();
        this.metrics =
                metrics.init(
                        this, MetricId.MetricLabels.from(ImmutableMap.of("provider", provider)));
    }

    public RefreshableItem getRefreshableItem() {
        return item;
    }

    @Override
    public String getMetricName() {
        return METRIC_NAME;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        try {
            log.info(
                    "[Restrict] Restrictions for credentialsId: {} are: {}",
                    context.getRequest().getCredentials().getId(),
                    dataFetchingRestrictions);
            if (isNotAllowedToRefreshItem()) {
                log.info(
                        "Item: {} is restricted from refresh - restrictions: {}, credentialsId: {}",
                        item,
                        dataFetchingRestrictions,
                        context.getRequest().getCredentials().getId());
                return AgentWorkerCommandResult.CONTINUE;
            }
        } catch (RuntimeException e) {
            log.warn("[Restrict] Failed: ", e);
        }

        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        try {
            MetricAction action =
                    metrics.buildAction(new MetricId.MetricLabels().add("action", METRIC_ACTION));
            try {
                log.info("Refreshing item: {}", item.name());

                Agent agent = context.getAgent();
                if (agent instanceof DeprecatedRefreshExecutor) {
                    ((DeprecatedRefreshExecutor) agent).refresh();
                } else {
                    RefreshExecutorUtils.executeSegregatedRefresher(agent, item, context);
                }

                if (isAbleToRefreshItem(agent, item)) {
                    action.completed();
                }
            } catch (BankServiceException e) {
                // The way frontend works now the message will not be displayed to the user.
                context.updateStatus(
                        CredentialsStatus.UNCHANGED,
                        context.getCatalog().getString(e.getUserMessage()));
                action.unavailable();
                refreshEventProducer.sendEventForRefreshWithErrorInBankSide(
                        context.getRequest().getProvider().getName(),
                        context.getCorrelationId(),
                        context.getRequest().getProvider().getMarket(),
                        context.getRequest().getCredentials().getId(),
                        context.getAppId(),
                        context.getClusterId(),
                        context.getRequest().getCredentials().getUserId(),
                        ADDITIONAL_INFO_ERROR_MAPPER.get(e.getError()),
                        item);
                log.warn(
                        "BankServiceException is received and credentials status set UNCHANGED.",
                        e);
                return AgentWorkerCommandResult.ABORT;
            } catch (java.lang.NullPointerException e) {
                log.warn(
                        "Couldn't refresh RefreshableItem({}) because of NullPointerException.",
                        item,
                        e);
                action.failed();
                refreshEventProducer.sendEventForRefreshWithErrorInTinkSide(
                        context.getRequest().getProvider().getName(),
                        context.getCorrelationId(),
                        context.getRequest().getProvider().getMarket(),
                        context.getRequest().getCredentials().getId(),
                        context.getAppId(),
                        context.getClusterId(),
                        context.getRequest().getCredentials().getUserId(),
                        AdditionalInfo.INTERNAL_SERVER_ERROR,
                        item);
                throw e;
            } catch (Exception e) {

                log.warn("Couldn't refresh RefreshableItem({}) because of exception.", item, e);
                action.failed();
                refreshEventProducer.sendEventForRefreshWithErrorInTinkSide(
                        context.getRequest().getProvider().getName(),
                        context.getCorrelationId(),
                        context.getRequest().getProvider().getMarket(),
                        context.getRequest().getCredentials().getId(),
                        context.getAppId(),
                        context.getClusterId(),
                        context.getRequest().getCredentials().getUserId(),
                        AdditionalInfo.ERROR_INFO,
                        item);
                throw e;
            }
        } finally {
            metrics.stop();
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    private boolean isNotAllowedToRefreshItem() {
        return customerDataFetchingRestrictions.shouldBeRestricted(item, dataFetchingRestrictions);
    }

    private boolean isAbleToRefreshItem(Agent agent, RefreshableItem item) {
        switch (item) {
            case ACCOUNTS:
            case TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS:
                return true;
            case EINVOICES:
                return agent instanceof RefreshEInvoiceExecutor;
            case TRANSFER_DESTINATIONS:
                return agent instanceof RefreshTransferDestinationExecutor;
            case CHECKING_ACCOUNTS:
            case CHECKING_TRANSACTIONS:
                return agent instanceof RefreshCheckingAccountsExecutor;
            case SAVING_ACCOUNTS:
            case SAVING_TRANSACTIONS:
                return agent instanceof RefreshSavingsAccountsExecutor;
            case CREDITCARD_ACCOUNTS:
            case CREDITCARD_TRANSACTIONS:
                return agent instanceof RefreshCreditCardAccountsExecutor;
            case LOAN_ACCOUNTS:
            case LOAN_TRANSACTIONS:
                return agent instanceof RefreshLoanAccountsExecutor;
            case INVESTMENT_ACCOUNTS:
            case INVESTMENT_TRANSACTIONS:
                return agent instanceof RefreshInvestmentAccountsExecutor;
            case IDENTITY_DATA:
                return agent instanceof RefreshIdentityDataExecutor;
            case LIST_BENEFICIARIES:
                return agent instanceof RefreshBeneficiariesExecutor;
            default:
                return false;
        }
    }

    private void sendIdentityToAgentDataAvailabilityTracker() {
        if (Strings.isNullOrEmpty(market)) {
            return;
        }

        if (context.getCachedIdentityData() == null) {
            log.info(
                    "Identity data is null, skipping identity data request to AgentDataAvailabilityTracker");
            return;
        }

        log.info("Sending Identity to AgentDataAvailabilityTracker");

        agentDataAvailabilityTrackerClient.sendIdentityData(
                agentName, provider, market, context.getAggregationIdentityData());

        IdentityDataSerializer serializer =
                agentDataAvailabilityTrackerClient.serializeIdentityData(
                        context.getAggregationIdentityData());

        List<Pair<String, Boolean>> eventData = new ArrayList<>();

        serializer
                .buildList()
                .forEach(
                        entry ->
                                eventData.add(
                                        new Pair<String, Boolean>(
                                                entry.getName(),
                                                !entry.getValue().equalsIgnoreCase("null"))));

        dataTrackerEventProducer.sendDataTrackerEvent(
                context.getRequest().getCredentials().getProviderName(),
                context.getCorrelationId(),
                eventData,
                context.getAppId(),
                context.getClusterId(),
                context.getRequest().getCredentials().getUserId());
    }

    @Override
    protected void doPostProcess() throws Exception {
        if (getRefreshableItem() == RefreshableItem.IDENTITY_DATA) {
            try {
                sendIdentityToAgentDataAvailabilityTracker();

                context.sendIdentityToIdentityAggregatorService();

            } catch (Exception e) {
                log.warn("Couldn't send Identity");

                throw e;
            }
        }
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add("class", RefreshItemAgentWorkerCommand.class.getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
