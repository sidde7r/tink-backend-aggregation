package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountBalanceType;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.metrics.core.MetricId;

public class SendAccountsToUpdateServiceAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(SendAccountsToUpdateServiceAgentWorkerCommand.class);

    private static final String METRIC_NAME = "agent_refresh";
    private static final String METRIC_ACTION = "send_accounts";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;

    public SendAccountsToUpdateServiceAgentWorkerCommand(
            AgentWorkerCommandContext context, AgentWorkerCommandMetricState metrics) {
        this.context = context;
        this.metrics = metrics.init(this);
    }

    @Override
    public String getMetricName() {
        return METRIC_NAME;
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        try {
            MetricAction action =
                    metrics.buildAction(new MetricId.MetricLabels().add("action", METRIC_ACTION));
            try {
                // Add balance computation to here
                /*
                   Requirements to execute:

                   1- Agent should tell us that we need to execute the computation
                   by setting a flag in Context
                   2- We should have list of transactions for account (if we don't have
                   either the current balance is accurate or we cannot do any better so
                   skipping is fine)
                */
                //

                // TODO (AAP-1566): Example implementation, check it and implement properly
                for (AccountData account : context.getAccountDataCache().getFilteredAccountData()) {
                    if (Objects.isNull(account.getAccount().getExactBalance())
                            && account.getTransactions().size() > 0) {
                        // TODO (AAP-1566): Also check that we have necessary ISO-balances in
                        // granular
                        // balances, otherwise don't execute the computation.
                        Map<AccountBalanceType, ExactCurrencyAmount> granularBalances =
                                account.getAccount().getGranularAccountBalances();
                        log.info(
                                "[BOOKED BALANCE AUTO COMPUTATION] Computation started for an account");
                        ExactCurrencyAmount computedBookedBalance =
                                ExactCurrencyAmount.of(
                                        account.getAccount().getAvailableBalance().getExactValue(),
                                        account.getAccount()
                                                .getAvailableBalance()
                                                .getCurrencyCode());
                        List<Transaction> pendingTransactions =
                                account.getTransactions().stream()
                                        .filter(Transaction::isPending)
                                        .collect(Collectors.toList());
                        for (Transaction transaction : pendingTransactions) {
                            computedBookedBalance.subtract(transaction.getTransactionAmount());
                        }
                        account.getAccount().setExactBalance(computedBookedBalance);
                        // TODO (AAP-1566): Check if we should also call
                        // account.getAccount().setBalance(...);
                    }
                }
            } catch (Exception e) {
                log.warn("Couldn't execute the balance computation");
            }

            try {
                log.info("Sending accounts to UpdateService");

                context.sendAllCachedAccountsToUpdateService();

                action.completed();
            } catch (Exception e) {
                action.failed();
                log.warn("Couldn't send Accounts to UpdateService");

                throw e;
            }
        } finally {
            metrics.stop();
        }

        return AgentWorkerCommandResult.CONTINUE;
    }

    @Override
    protected void doPostProcess() throws Exception {
        // Deliberately left empty.
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add(
                                "class",
                                SendAccountsToUpdateServiceAgentWorkerCommand.class.getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
