package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.DeprecatedRefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshExecutor;
import se.tink.backend.aggregation.agents.RefreshableItemExecutor;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.rpc.RefreshableItem;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.RefreshMetricNameFactory;
import se.tink.backend.system.rpc.AccountFeatures;
import se.tink.backend.system.rpc.Transaction;
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
                if (agent instanceof RefreshExecutor) {
                    switch (item) {
                        case EINVOICES:
                            context.updateEinvoices(
                                    ((RefreshExecutor) agent).fetchEInvoices().getEInvoices()
                            );
                            break;
                        case TRANSFER_DESTINATIONS:
                            context.updateTransferDestinationPatterns(
                                    ((RefreshExecutor) agent)
                                            .fetchTransferDestinations(context.getUpdatedAccounts())
                                            .getTransferDestinations());
                            break;
                        case CHECKING_ACCOUNTS:
                            context.cacheAccounts(
                                    ((RefreshExecutor) agent)
                                            .fetchCheckingAccounts()
                                            .getAccounts());
                            break;
                        case CHECKING_TRANSACTIONS:
                            for (Map.Entry<String, List<Transaction>> accountTransactions :
                                    ((RefreshExecutor) agent)
                                            .fetchCheckingTransactions()
                                            .getTransactions()
                                            .entrySet()) {
                                context.cacheTransactions(
                                        accountTransactions.getKey(),
                                        accountTransactions.getValue());
                            }
                            break;
                        case SAVINGS_ACCOUNTS:
                            context.cacheAccounts(
                                    ((RefreshExecutor) agent)
                                            .fetchSavingsAccounts()
                                            .getAccounts());
                            break;
                        case SAVINGS_TRANSACTIONS:
                            for (Map.Entry<String, List<Transaction>> accountTransactions :
                                    ((RefreshExecutor) agent)
                                            .fetchSavingsTransactions()
                                            .getTransactions()
                                            .entrySet()) {
                                context.cacheTransactions(
                                        accountTransactions.getKey(),
                                        accountTransactions.getValue());
                            }
                            break;
                        case CREDITCARD_ACCOUNTS:
                            context.cacheAccounts(
                                    ((RefreshExecutor) agent)
                                            .fetchCreditCardAccounts()
                                            .getAccounts());
                            break;
                        case CREDITCARD_TRANSACTIONS:
                            for (Map.Entry<String, List<Transaction>> accountTransactions :
                                    ((RefreshExecutor) agent)
                                            .fetchCreditCardTransactions()
                                            .getTransactions()
                                            .entrySet()) {
                                context.cacheTransactions(
                                        accountTransactions.getKey(),
                                        accountTransactions.getValue());
                            }
                            break;
                        case LOAN_ACCOUNTS:
                            for (Map.Entry<Account, AccountFeatures> loanAccount :
                                    ((RefreshExecutor) agent)
                                            .fetchLoanAccounts()
                                            .getAccounts()
                                            .entrySet()) {
                                context.cacheAccount(loanAccount.getKey(), loanAccount.getValue());
                            }
                            break;
                        case INVESTMENT_ACCOUNTS:
                            for (Map.Entry<Account, AccountFeatures> loanAccount :
                                    ((RefreshExecutor) agent)
                                            .fetchInvestmentAccounts()
                                            .getAccounts()
                                            .entrySet()) {
                                context.cacheAccount(loanAccount.getKey(), loanAccount.getValue());
                            }
                            break;
                        case ACCOUNTS:
                        case TRANSACTIONAL_ACCOUNTS_AND_TRANSACTIONS:
                            throw new IllegalStateException("Legacy types should not be refreshed");
                        case LOAN_TRANSACTIONS:
                        case INVESTMENT_TRANSACTIONS:
                            throw new NotImplementedException(String.format("No implementation for %s", item.name()));
                    }
                } else if (agent instanceof RefreshableItemExecutor) {
                    ((RefreshableItemExecutor) agent).refresh(item);
                } else {
                    ((DeprecatedRefreshExecutor) agent).refresh();
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
