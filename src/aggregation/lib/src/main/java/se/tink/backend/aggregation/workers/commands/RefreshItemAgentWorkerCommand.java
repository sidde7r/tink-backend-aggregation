package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.RefreshMetricNameFactory;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.metrics.core.MetricId;

public class RefreshItemAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final Logger log = LoggerFactory.getLogger(RefreshItemAgentWorkerCommand.class);

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "refresh";

    private final AgentWorkerCommandContext context;
    private final RefreshableItem item;
    private final AgentWorkerCommandMetricState metrics;
    private final AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient;
    private final DataTrackerEventProducer dataTrackerEventProducer;

    private final String agentName;
    private final String provider;
    private final String market;

    public RefreshItemAgentWorkerCommand(
            AgentWorkerCommandContext context,
            RefreshableItem item,
            AgentWorkerCommandMetricState metrics,
            AgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient,
            DataTrackerEventProducer dataTrackerEventProducer) {
        this.context = context;
        this.item = item;
        this.metrics = metrics.init(this);
        this.agentDataAvailabilityTrackerClient = agentDataAvailabilityTrackerClient;
        this.dataTrackerEventProducer = dataTrackerEventProducer;

        CredentialsRequest request = context.getRequest();

        this.agentName = request.getProvider().getClassName();
        this.provider = request.getProvider().getName();
        this.market = request.getProvider().getMarket();
    }

    public RefreshableItem getRefreshableItem() {
        return item;
    }

    @Override
    public String getMetricName() {
        return METRIC_NAME;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        try {
            MetricAction action =
                    metrics.buildAction(
                            new MetricId.MetricLabels()
                                    .add("action", METRIC_ACTION)
                                    .add("item", item.name()));
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
                return AgentWorkerCommandResult.ABORT;
            } catch (Exception e) {
                action.failed();
                log.warn("Couldn't refresh RefreshableItem({})", item);

                throw e;
            }
        } finally {
            metrics.stop();
        }

        return AgentWorkerCommandResult.CONTINUE;
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
    }

    @Override
    public void postProcess() throws Exception {
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
                        .add("item", RefreshMetricNameFactory.createCleanName(item))
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
