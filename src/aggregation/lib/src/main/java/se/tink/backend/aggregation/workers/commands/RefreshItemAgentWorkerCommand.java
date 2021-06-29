package se.tink.backend.aggregation.workers.commands;

import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.ACCESS_EXCEEDED;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.BANK_SIDE_FAILURE;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.MULTIPLE_LOGIN;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.NO_BANK_SERVICE;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.SESSION_TERMINATED;
import static se.tink.backend.aggregation.agents.exceptions.errors.SessionError.CONSENT_EXPIRED;
import static se.tink.backend.aggregation.agents.exceptions.errors.SessionError.CONSENT_INVALID;
import static se.tink.backend.aggregation.agents.exceptions.errors.SessionError.CONSENT_REVOKED;
import static se.tink.backend.aggregation.agents.exceptions.errors.SessionError.CONSENT_REVOKED_BY_USER;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
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
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshStatus;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshSummary;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshableItemFetchingStatus;
import se.tink.backend.aggregation.compliance.customer_restrictions.CustomerDataFetchingRestrictions;
import se.tink.backend.aggregation.events.RefreshEvent;
import se.tink.backend.aggregation.events.RefreshEventProducer;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.eventproducerservice.events.grpc.RefreshResultEventProto.RefreshResultEvent.AdditionalInfo;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.DataFetchingRestrictions;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.metrics.core.MetricId;
import src.libraries.connectivity_errors.ConnectivityErrorFactory;

@Slf4j
public class RefreshItemAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "refresh";

    private final AgentWorkerCommandContext context;
    private final RefreshableItem item;
    private final AgentWorkerCommandMetricState metrics;
    private final RefreshEventProducer refreshEventProducer;
    private final List<DataFetchingRestrictions> dataFetchingRestrictions;
    private final CustomerDataFetchingRestrictions customerDataFetchingRestrictions =
            new CustomerDataFetchingRestrictions();

    private final String provider;
    private static final ImmutableMap<BankServiceError, AdditionalInfo>
            ADDITIONAL_INFO_ERROR_MAPPER =
                    ImmutableMap.<BankServiceError, AdditionalInfo>builder()
                            .put(ACCESS_EXCEEDED, AdditionalInfo.ACCESS_EXCEEDED)
                            .put(BANK_SIDE_FAILURE, AdditionalInfo.BANK_SIDE_FAILURE)
                            .put(MULTIPLE_LOGIN, AdditionalInfo.MULTIPLE_LOGIN)
                            .put(NO_BANK_SERVICE, AdditionalInfo.NO_BANK_SERVICE)
                            .put(SESSION_TERMINATED, AdditionalInfo.SESSION_TERMINATED)
                            .build();

    private static final ImmutableMap<SessionError, AdditionalInfo>
            ADDITIONAL_INFO_SESSION_ERROR_MAPPER =
                    ImmutableMap.<SessionError, AdditionalInfo>builder()
                            .put(CONSENT_EXPIRED, AdditionalInfo.CONSENT_EXPIRED)
                            .put(CONSENT_INVALID, AdditionalInfo.CONSENT_INVALID)
                            .put(CONSENT_REVOKED_BY_USER, AdditionalInfo.CONSENT_REVOKED)
                            .put(CONSENT_REVOKED, AdditionalInfo.CONSENT_REVOKED)
                            .build();

    public RefreshItemAgentWorkerCommand(
            AgentWorkerCommandContext context,
            RefreshableItem item,
            AgentWorkerCommandMetricState metrics,
            RefreshEventProducer refreshEventProducer) {
        this.context = context;
        this.item = item;
        this.refreshEventProducer = refreshEventProducer;
        CredentialsRequest request = context.getRequest();
        this.provider = request.getProvider().getName();
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
        RefreshSummary refreshSummary = context.getRefreshSummary();
        if (refreshSummary == null) {
            refreshSummary = new RefreshSummary(context.getRequest(), context.getAppId());
            context.setRefreshSummary(refreshSummary);
        }

        if (isNotAllowedToRefresh()) {
            refreshSummary.addItemSummary(item, RefreshableItemFetchingStatus.RESTRICTED);
            return AgentWorkerCommandResult.CONTINUE;
        }

        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);

        try {
            MetricAction action =
                    metrics.buildAction(
                            new MetricId.MetricLabels()
                                    .add("action", METRIC_ACTION)
                                    .add("type", item.name()));

            try {
                Agent agent = context.getAgent();

                refreshSummary.updateStatus(RefreshStatus.FETCHING_STARTED);
                boolean allItemsRefreshedSuccessfully = executeRefresh(agent);

                if (isAbleToRefreshItem(agent, item)) {
                    if (allItemsRefreshedSuccessfully) {
                        action.completed();
                        refreshSummary.updateStatus(RefreshStatus.FETCHING_COMPLETED);
                    } else {
                        action.partiallyCompleted();
                        refreshSummary.updateStatus(RefreshStatus.FETCHING_COMPLETED_PARTIALLY);
                    }
                }

            } catch (BankServiceException e) {
                refreshSummary.updateStatus(RefreshStatus.INTERRUPTED_BY_BANK_SERVICE_EXCEPTION);
                handleFailedRefreshDueToBankError(action, e);
                return AgentWorkerCommandResult.ABORT;

            } catch (SessionException e) {
                refreshSummary.updateStatus(RefreshStatus.INTERRUPTED_BY_SESSION_EXCEPTION);
                handleFailedRefreshDueToSessionError(action, e);
                return AgentWorkerCommandResult.ABORT;

            } catch (RuntimeException e) {
                log.warn(
                        "Couldn't refresh RefreshableItem({}) because of RuntimeException.",
                        item,
                        e);
                refreshSummary.updateStatus(RefreshStatus.INTERRUPTED_BY_RUNTIME_EXCEPTION);
                handleFailedRefreshDueToTinkException(
                        action, e, AdditionalInfo.INTERNAL_SERVER_ERROR);
            } catch (Exception e) {
                log.warn("Couldn't refresh RefreshableItem({}) because of exception.", item, e);
                refreshSummary.updateStatus(RefreshStatus.INTERRUPTED_BY_EXCEPTION);
                handleFailedRefreshDueToTinkException(action, e, AdditionalInfo.ERROR_INFO);
            }
        } finally {
            metrics.stop();
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    private boolean isNotAllowedToRefresh() {
        try {
            if (dataFetchingRestrictions.size() > 0) {
                log.info(
                        "[REFRESH ITEM COMMAND] Restrictions for credentialsId: {} are: {}",
                        context.getRequest().getCredentials().getId(),
                        dataFetchingRestrictions);
            }
            if (isNotAllowedToRefreshItem()) {
                log.info(
                        "[REFRESH ITEM COMMAND] {} is restricted from refresh - restrictions: {}, credentialsId: {}",
                        item,
                        dataFetchingRestrictions,
                        context.getRequest().getCredentials().getId());
                return true;
            }
        } catch (RuntimeException e) {
            log.warn("[REFRESH ITEM COMMAND] Restricting item failed: ", e);
        }
        return false;
    }

    /*
     * returns if all items were successfully refreshed
     */
    private boolean executeRefresh(Agent agent) throws Exception {
        if (agent instanceof DeprecatedRefreshExecutor) {
            ((DeprecatedRefreshExecutor) agent).refresh();
            return true;
        } else {
            return RefreshExecutorUtils.executeSegregatedRefresher(agent, item, context);
        }
    }

    private void handleFailedRefreshDueToBankError(MetricAction action, BankServiceException e) {
        // The way frontend works now the message will not be displayed to the user.
        context.updateStatusWithError(
                CredentialsStatus.TEMPORARY_ERROR,
                context.getCatalog().getString(e.getUserMessage()),
                ConnectivityErrorFactory.fromLegacy(e));
        action.unavailable();
        AdditionalInfo errorInfo = ADDITIONAL_INFO_ERROR_MAPPER.get(e.getError());
        RefreshEvent refreshEvent = getRefreshEvent(errorInfo);
        refreshEventProducer.sendEventForRefreshWithErrorInBankSide(refreshEvent);
        log.warn(
                "[REFRESH ITEM COMMAND] Due to received bank error credentials status set TEMPORARY_ERROR.",
                e);
    }

    private void handleFailedRefreshDueToSessionError(MetricAction action, SessionException e) {
        // The way frontend works now the message will not be displayed to the user.
        context.updateStatusWithError(
                CredentialsStatus.TEMPORARY_ERROR,
                context.getCatalog().getString(e.getUserMessage()),
                ConnectivityErrorFactory.fromLegacy(e));
        action.unavailable();
        AdditionalInfo errorInfo = ADDITIONAL_INFO_SESSION_ERROR_MAPPER.get(e.getError());
        RefreshEvent refreshEvent = getRefreshEvent(errorInfo);
        refreshEventProducer.sendEventForRefreshWithErrorInBankSide(refreshEvent);
        log.warn(
                "[REFRESH ITEM COMMAND] Due to session error credentials status set TEMPORARY_ERROR.",
                e);
    }

    private void handleFailedRefreshDueToTinkException(
            MetricAction action, Exception e, AdditionalInfo errorInfo) throws Exception {
        action.failed();
        RefreshEvent refreshEvent = getRefreshEvent(errorInfo);
        refreshEventProducer.sendEventForRefreshWithErrorInTinkSide(refreshEvent);
        throw e;
    }

    private RefreshEvent getRefreshEvent(AdditionalInfo errorInfo) {
        return RefreshEvent.builder()
                .providerName(context.getRequest().getProvider().getName())
                .correlationId(context.getCorrelationId())
                .marketCode(context.getRequest().getProvider().getMarket())
                .credentialsId(context.getRequest().getCredentials().getId())
                .appId(context.getAppId())
                .clusterId(context.getClusterId())
                .userId(context.getRequest().getCredentials().getUserId())
                .additionalInfo(errorInfo)
                .refreshableItem(item)
                .build();
    }

    private boolean isNotAllowedToRefreshItem() {
        // if non-account then we just want to fetch if it's not restricted
        if (!RefreshableItem.isAccount(item)) {
            return customerDataFetchingRestrictions.shouldBeRestricted(
                    item, dataFetchingRestrictions);
        }
        // if account then we may want to fetch regardless if it's restricted (to send some events)
        boolean allowRefreshRegardlessOfRestrictions =
                context.getAgentsServiceConfiguration()
                        .isFeatureEnabled("allowRefreshRegardlessOfRestrictions");
        return !allowRefreshRegardlessOfRestrictions
                && customerDataFetchingRestrictions.shouldBeRestricted(
                        item, dataFetchingRestrictions);
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

    @Override
    protected void doPostProcess() throws Exception {
        if (getRefreshableItem() == RefreshableItem.IDENTITY_DATA) {
            try {
                context.sendIdentityToIdentityAggregatorService();
            } catch (Exception e) {
                log.warn("[REFRESH ITEM COMMAND] Couldn't send Identity");
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
