package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshEInvoiceExecutor;
import se.tink.backend.aggregation.agents.RefreshExecutorUtils;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshTransferDestinationExecutor;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.RefreshMetricNameFactory;
import se.tink.libraries.metrics.MetricId;

public class RefreshItemAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final Logger log = LoggerFactory.getLogger(RefreshItemAgentWorkerCommand.class);

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "refresh";


    private final AgentWorkerCommandContext context;
    private final RefreshableItem item;
    private final AgentWorkerCommandMetricState metrics;

    public RefreshItemAgentWorkerCommand(AgentWorkerCommandContext context, RefreshableItem item,
            AgentWorkerCommandMetricState metrics) {
        this.context = context;
        this.item = item;
        this.metrics = metrics.init(this);
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
            MetricAction action = metrics.buildAction(
                    new MetricId.MetricLabels()
                            .add("action", METRIC_ACTION)
                            .add("item", item.name())
            );
            try {
                log.info("Refreshing item: {}", item.name());

                Agent agent = context.getAgent();
                if (agent instanceof RefreshableItemExecutor) {
                    ((RefreshableItemExecutor) agent).refresh(item);
                } else if (agent instanceof DeprecatedRefreshExecutor){
                    ((DeprecatedRefreshExecutor) agent).refresh();
                } else {
                    executeSegregatedRefresher(agent, item);
                }
                action.completed();
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

    private void executeSegregatedRefresher(Agent agent, RefreshableItem item) {
        Class executorKlass = RefreshExecutorUtils.getRefreshExecutor(item);
        if (executorKlass == null) {
            throw new NotImplementedException(String.format("No implementation for %s", item.name()));
        }
        // Segregated refresh executor
        if (executorKlass.isAssignableFrom(agent.getAgentClass())) {
            switch (item) {
                case EINVOICES:
                    context.updateEinvoices(((RefreshEInvoiceExecutor) agent).fetchEInvoices().getEInvoices());
                    break;
                case TRANSFER_DESTINATIONS:
                    context.updateTransferDestinationPatterns(
                            ((RefreshTransferDestinationExecutor) agent)
                                    .fetchTransferDestinations(context.getUpdatedAccounts())
                                    .getTransferDestinations());
                    break;
                case CHECKING_ACCOUNTS:
                    context.cacheAccounts(((RefreshCheckingAccountsExecutor) agent).fetchCheckingAccounts().getAccounts());
                    break;
                case CHECKING_TRANSACTIONS:
                    ((RefreshCheckingAccountsExecutor) agent)
                            .fetchCheckingTransactions()
                            .getTransactions()
                            .forEach((key, value) -> context.updateTransactions(key, value));
                    break;
                case SAVING_ACCOUNTS:
                    context.cacheAccounts(((RefreshSavingsAccountsExecutor) agent).fetchSavingsAccounts().getAccounts());
                    break;
                case SAVING_TRANSACTIONS:
                    ((RefreshSavingsAccountsExecutor) agent)
                            .fetchSavingsTransactions()
                            .getTransactions()
                            .forEach((key, value) -> context.updateTransactions(key, value));

                    break;
                case CREDITCARD_ACCOUNTS:
                    context.cacheAccounts(
                            ((RefreshCreditCardAccountsExecutor) agent).fetchCreditCardAccounts().getAccounts());
                    break;
                case CREDITCARD_TRANSACTIONS:

                    ((RefreshCreditCardAccountsExecutor) agent)
                            .fetchCreditCardTransactions()
                            .getTransactions()
                            .forEach((key, value) -> context.updateTransactions(key, value));
                    break;
                case LOAN_ACCOUNTS:
                    ((RefreshLoanAccountsExecutor) agent)
                            .fetchLoanAccounts()
                            .getAccounts()
                            .forEach((key, value) -> context.cacheAccount(key, value));
                    break;
                case LOAN_TRANSACTIONS:
                    ((RefreshLoanAccountsExecutor) agent)
                            .fetchLoanTransactions()
                            .getTransactions()
                            .forEach((key, value) -> context.updateTransactions(key, value));
                    break;
                case INVESTMENT_ACCOUNTS:
                    ((RefreshInvestmentAccountsExecutor) agent)
                            .fetchInvestmentAccounts()
                            .getAccounts()
                            .forEach((key, value) -> context.cacheAccount(key, value));
                    break;
                case INVESTMENT_TRANSACTIONS:
                    ((RefreshInvestmentAccountsExecutor) agent)
                            .fetchInvestmentTransactions()
                            .getTransactions()
                            .forEach((key, value) -> context.updateTransactions(key, value));
                    break;
                default:
                    throw new IllegalStateException(
                            String.format("Invalid refreshable item detected %s", item.name()));
            }
        }
    }

    @Override
    public void postProcess() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName = new MetricId.MetricLabels()
                .add("class", RefreshItemAgentWorkerCommand.class.getSimpleName())
                .add("item", RefreshMetricNameFactory.createCleanName(item))
                .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
