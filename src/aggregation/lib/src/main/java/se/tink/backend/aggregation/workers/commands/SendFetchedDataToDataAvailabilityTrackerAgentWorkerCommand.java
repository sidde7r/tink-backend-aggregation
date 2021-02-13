package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.IdentityDataSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.SerializationUtils;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.TransactionTrackingSerializer;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.pair.Pair;

public class SendFetchedDataToDataAvailabilityTrackerAgentWorkerCommand extends AgentWorkerCommand
        implements MetricsCommand {
    private static final Logger log =
            LoggerFactory.getLogger(
                    SendFetchedDataToDataAvailabilityTrackerAgentWorkerCommand.class);

    private static final String METRIC_NAME = "data_availability_tracker_refresh";
    private static final String METRIC_ACTION = "send_refresh_data_to_data_availability_tracker";

    private final AgentWorkerCommandContext context;
    private final AgentWorkerCommandMetricState metrics;

    private final AsAgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient;
    private final DataTrackerEventProducer dataTrackerEventProducer;

    private final String agentName;
    private final String provider;
    private final String market;

    private static final int MAX_TRANSACTIONS_TO_SEND_TO_BIGQUERY_PER_ACCOUNT = 10;

    public SendFetchedDataToDataAvailabilityTrackerAgentWorkerCommand(
            AgentWorkerCommandContext context,
            AgentWorkerCommandMetricState metrics,
            AsAgentDataAvailabilityTrackerClient agentDataAvailabilityTrackerClient,
            DataTrackerEventProducer dataTrackerEventProducer) {
        this.context = context;
        this.metrics = metrics.init(this);
        this.agentDataAvailabilityTrackerClient = agentDataAvailabilityTrackerClient;
        this.dataTrackerEventProducer = dataTrackerEventProducer;
        CredentialsRequest request = context.getRequest();

        this.agentName = request.getProvider().getClassName();
        this.provider = request.getProvider().getName();
        this.market = request.getProvider().getMarket();
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
                if (!Strings.isNullOrEmpty(market)) {
                    // Handles process for accounts and their transactions
                    context.getCachedAccountsWithFeatures()
                            .forEach(pair -> processForDataTracker(pair.first, pair.second));
                    sendIdentityToAgentDataAvailabilityTracker();
                    action.completed();
                } else {
                    action.cancelled();
                }
            } catch (Exception e) {
                action.failed();
                log.error("Failed sending refresh data to tracking service.", e);
            }
        } finally {
            metrics.stop();
        }
        return AgentWorkerCommandResult.CONTINUE;
    }

    private void processForDataTracker(final Account account, final AccountFeatures features) {
        AccountTrackingSerializer serializer =
                SerializationUtils.serializeAccount(account, features);

        /*
           We are intentionally sending only account and skipping transaction to data-tracker.
           We are sending transactions only as data-tracker-event to BigQuery. We are planning
           to deprecate data-tracker and only use BigQuery and for this reason we are following
           such an approach for transactions.
        */
        agentDataAvailabilityTrackerClient.sendAccount(agentName, provider, market, serializer);

        // Sending data to BigQuery by emitting events
        sendAccountToBigQuery(account, features);

        /*
           For an account we will pick the most recent MAX_TRANSACTIONS_TO_SEND_TO_BIGQUERY_PER_ACCOUNT
           transactions. We do not want to send all transactions in order to avoid putting too much
           load to the system
        */
        try {
            List<Transaction> originalTransactions =
                    context.getAccountDataCache()
                            .getTransactionsByAccountToBeProcessed()
                            .get(account);
            boolean foundTransactions = false;
            if (Objects.isNull(originalTransactions)) {
                log.info(
                        String.format(
                                "Could not get transactions of account to send to BigQuery. Account type is %s",
                                account.getType().toString()));
                originalTransactions = getTransactionsForAccount(account);
                if (Objects.isNull(originalTransactions)) {
                    log.info(
                            String.format(
                                    "Could not get transactions of account again to send to BigQuery. Account type is %s",
                                    account.getType().toString()));
                } else {
                    foundTransactions = true;
                    log.info("getTransactionsForAccount method worked!");
                }
            } else {
                foundTransactions = true;
            }
            if (foundTransactions) {
                log.info(
                        String.format(
                                "We have transactions for account to send to BQ. Account type is %s",
                                account.getType().toString()));
                List<Transaction> transactionsOfAccount = new ArrayList<>(originalTransactions);
                Collections.shuffle(transactionsOfAccount);
                List<Transaction> transactionsToProcess =
                        transactionsOfAccount.size()
                                        <= MAX_TRANSACTIONS_TO_SEND_TO_BIGQUERY_PER_ACCOUNT
                                ? transactionsOfAccount
                                : transactionsOfAccount.subList(
                                        0, MAX_TRANSACTIONS_TO_SEND_TO_BIGQUERY_PER_ACCOUNT);
                transactionsToProcess.forEach(
                        transaction -> sendTransactionToBigQuery(transaction, account.getType()));
            }
        } catch (Exception e) {
            // This is set to info temporarily. Normally the level of this log should be "warn"
            log.info("Failed to send transaction data to BigQuery", e);
        }
    }

    private List<Transaction> getTransactionsForAccount(Account account) {
        return context.getAccountDataCache().getFilteredAccountData().stream()
                .collect(Collectors.toMap(AccountData::getAccount, AccountData::getTransactions))
                .get(account);
    }

    private void sendAccountToBigQuery(final Account account, final AccountFeatures features) {
        AccountTrackingSerializer serializer =
                SerializationUtils.serializeAccount(account, features);

        List<Pair<String, Boolean>> eventData = new ArrayList<>();

        serializer
                .buildList()
                .forEach(
                        entry -> {
                            if (entry.getName().endsWith(".identifiers")) {
                                eventData.add(
                                        new Pair<String, Boolean>(
                                                entry.getName()
                                                        + "."
                                                        + entry.getValue().replace("-", ""),
                                                true));
                            } else {
                                eventData.add(
                                        new Pair<String, Boolean>(
                                                entry.getName(),
                                                !("null".equalsIgnoreCase(entry.getValue()))));
                            }
                        });

        dataTrackerEventProducer.sendDataTrackerEvent(
                context.getRequest().getCredentials().getProviderName(),
                context.getCorrelationId(),
                eventData,
                context.getAppId(),
                context.getClusterId(),
                context.getRequest().getCredentials().getUserId());
    }

    private void sendTransactionToBigQuery(Transaction transaction, AccountTypes accountType) {
        TransactionTrackingSerializer serializer =
                new TransactionTrackingSerializer(transaction, accountType);

        List<Pair<String, Boolean>> eventData = new ArrayList<>();

        serializer
                .buildList()
                .forEach(
                        entry ->
                                eventData.add(
                                        new Pair<String, Boolean>(
                                                entry.getName(),
                                                !("null".equalsIgnoreCase(entry.getValue())))));

        dataTrackerEventProducer.sendDataTrackerEvent(
                context.getRequest().getCredentials().getProviderName(),
                context.getCorrelationId(),
                eventData,
                context.getAppId(),
                context.getClusterId(),
                context.getRequest().getCredentials().getUserId());
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

        IdentityDataSerializer serializer =
                SerializationUtils.serializeIdentityData(context.getAggregationIdentityData());

        agentDataAvailabilityTrackerClient.sendIdentityData(
                agentName, provider, market, serializer);

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
        // Deliberately left empty.
    }

    @Override
    public List<MetricId.MetricLabels> getCommandTimerName(AgentWorkerOperationMetricType type) {
        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add(
                                "class",
                                SendFetchedDataToDataAvailabilityTrackerAgentWorkerCommand.class
                                        .getSimpleName())
                        .add("command", type.getMetricName());

        return Lists.newArrayList(typeName);
    }
}
