package se.tink.backend.aggregation.workers.commands;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.AccountFeatures;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.backend.aggregation.events.DataTrackerEventProducer;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.integration.agent_data_availability_tracker.client.AsAgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.agent_data_availability_tracker.common.serialization.TrackingMapSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.AccountTrackingSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.IdentityDataSerializer;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.SerializationUtils;
import se.tink.backend.integration.agent_data_availability_tracker.serialization.TransactionTrackingSerializer;
import se.tink.eventproducerservice.events.grpc.DataTrackerEventProto.DataTrackerEvent;
import se.tink.libraries.account_data_cache.AccountData;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;

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
                    List<DataTrackerEvent> events = new ArrayList<>();
                    context.getCachedAccountsWithFeatures()
                            .forEach(
                                    pair ->
                                            events.addAll(
                                                    processAccountForDataTracker(
                                                            pair.first, pair.second)));
                    Optional<DataTrackerEvent> identityDataEvent =
                            produceIdentityDataEventForBigQuery();
                    identityDataEvent.ifPresent(events::add);
                    dataTrackerEventProducer.sendDataTrackerEvents(events);
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

    private List<DataTrackerEvent> processAccountForDataTracker(
            final Account account, final AccountFeatures features) {

        List<DataTrackerEvent> events = new ArrayList<>();

        AccountTrackingSerializer serializer =
                SerializationUtils.serializeAccount(account, features);

        /*
           We are intentionally sending only account and skipping transaction to data-tracker.
           We are sending transactions only as data-tracker-event to BigQuery. We are planning
           to deprecate data-tracker and only use BigQuery and for this reason we are following
           such an approach for transactions.
        */
        agentDataAvailabilityTrackerClient.sendAccount(agentName, provider, market, serializer);

        // Produce event data for BigQuery for Account
        events.add(produceDataTrackerEvent(SerializationUtils.serializeAccount(account, features)));

        /*
           For an account we will pick the most recent MAX_TRANSACTIONS_TO_SEND_TO_BIGQUERY_PER_ACCOUNT
           transactions. We do not want to send all transactions in order to avoid putting too much
           load to the system
        */
        try {
            List<Transaction> originalTransactions = getTransactionsForAccount(account);
            if (Objects.isNull(originalTransactions)) {
                log.info(
                        String.format(
                                "Could not get transactions of account to send to BigQuery. Account type is %s",
                                account.getType().toString()));
            } else {
                log.info(
                        String.format(
                                "We have transactions for account to send to BQ. Account type is %s",
                                account.getType().toString()));

                // Pick MAX_TRANSACTIONS_TO_SEND_TO_BIGQUERY_PER_ACCOUNT transactions randomly and
                // emit events for them
                List<Transaction> transactionsOfAccount = new ArrayList<>(originalTransactions);
                Collections.shuffle(transactionsOfAccount);
                Set<Transaction> transactionsToProcess =
                        new HashSet<>(
                                transactionsOfAccount.size()
                                                <= MAX_TRANSACTIONS_TO_SEND_TO_BIGQUERY_PER_ACCOUNT
                                        ? transactionsOfAccount
                                        : transactionsOfAccount.subList(
                                                0,
                                                MAX_TRANSACTIONS_TO_SEND_TO_BIGQUERY_PER_ACCOUNT));

                // On top of randomly selected transactions, ensure that we pick the oldest
                // transactions in terms of BOOKING_DATE, VALUE_DATE and EXECUTION_DATE
                // because we want to emit timestamp for these transactions to be able
                // to see the timestamp for the oldest transaction
                for (TransactionDateType transactionDateType : TransactionDateType.values()) {
                    Optional<Transaction> oldestTransaction =
                            findOldestTransactionByCriteria(
                                    transactionsOfAccount,
                                    transaction ->
                                            transaction.getDateForTransactionDateType(
                                                    transactionDateType));
                    if (oldestTransaction.isPresent()) {
                        log.info(
                                "Oldest transaction by {} is from {}",
                                transactionDateType.toString(),
                                oldestTransaction
                                        .get()
                                        .getDateForTransactionDateType(transactionDateType));
                        transactionsToProcess.add(oldestTransaction.get());
                    } else {
                        log.info(
                                "Could not detect oldest transaction for {}. The agent does not set {} for transactions",
                                transactionDateType.toString(),
                                transactionDateType.toString());
                    }
                }

                // We also want to find oldest transactions in terms of "timestamp" and "date"
                // fields and emit events for them as well
                Optional<Transaction> oldestTransactionByDate =
                        findOldestTransactionByCriteria(
                                transactionsOfAccount,
                                transaction -> Optional.ofNullable(transaction.getDate()));
                if (oldestTransactionByDate.isPresent()) {
                    log.info(
                            "Oldest transaction by date is from {}",
                            oldestTransactionByDate.get().getDate());
                    transactionsToProcess.add(oldestTransactionByDate.get());
                } else {
                    log.info(
                            "Could not detect oldest transaction for date field. The agent does not set date field for transactions");
                }

                Optional<Transaction> oldestTransactionByTimestamp =
                        findOldestTransactionByCriteria(
                                transactionsOfAccount,
                                transaction -> {
                                    long timestamp = transaction.getTimestamp();
                                    return timestamp == 0L
                                            ? Optional.empty()
                                            : Optional.of(timestamp);
                                });
                if (oldestTransactionByTimestamp.isPresent()) {
                    log.info(
                            "Oldest transaction by timestamp is from {}",
                            oldestTransactionByTimestamp.get().getTimestamp());
                    transactionsToProcess.add(oldestTransactionByTimestamp.get());
                } else {
                    log.info(
                            "Could not detect oldest transaction for timestamp field. The agent does not set timestamp field for transactions");
                }

                transactionsToProcess.forEach(
                        transaction ->
                                events.add(
                                        produceDataTrackerEvent(
                                                new TransactionTrackingSerializer(
                                                        transaction, account.getType()))));
            }
        } catch (Exception e) {
            log.warn(
                    "Failed to send transaction data to BigQuery. Cause: {}",
                    ExceptionUtils.getStackTrace(e));
        }

        return events;
    }

    private <T extends Comparable> Optional<Transaction> findOldestTransactionByCriteria(
            List<Transaction> transactions, Function<Transaction, Optional<T>> fieldValueGetter) {
        return transactions.stream()
                .filter(t -> fieldValueGetter.apply(t).isPresent())
                .min(
                        (t1, t2) -> {
                            T date1 =
                                    fieldValueGetter
                                            .apply(t1)
                                            .orElseThrow(
                                                    () ->
                                                            new IllegalStateException(
                                                                    "Field value getter failed"));
                            T date2 =
                                    fieldValueGetter
                                            .apply(t2)
                                            .orElseThrow(
                                                    () ->
                                                            new IllegalStateException(
                                                                    "Field value getter failed"));
                            return date1.compareTo(date2);
                        });
    }

    private List<Transaction> getTransactionsForAccount(Account account) {
        return context.getAccountDataCache().getFilteredAccountData().stream()
                .collect(Collectors.toMap(AccountData::getAccount, AccountData::getTransactions))
                .get(account);
    }

    private Optional<DataTrackerEvent> produceIdentityDataEventForBigQuery() {
        if (Strings.isNullOrEmpty(market)) {
            return Optional.empty();
        }

        if (context.getCachedIdentityData() == null) {
            log.info(
                    "Identity data is null, skipping identity data request to AgentDataAvailabilityTracker");
            return Optional.empty();
        }

        IdentityDataSerializer serializer =
                SerializationUtils.serializeIdentityData(context.getAggregationIdentityData());

        agentDataAvailabilityTrackerClient.sendIdentityData(
                agentName, provider, market, serializer);

        return Optional.of(produceDataTrackerEvent(serializer));
    }

    private DataTrackerEvent produceDataTrackerEvent(TrackingMapSerializer serializer) {
        // for each field keeps information on whether this field
        // is populated or not
        Map<String, Boolean> fieldsPopulated = new HashMap<>();
        Map<String, String> fieldValues = new HashMap<>();

        serializer
                .buildList()
                .forEach(
                        entry -> {
                            String fieldName = entry.getName();
                            String fieldValue = entry.getValue();
                            boolean isFieldPopulated = !("null".equalsIgnoreCase(fieldValue));
                            fieldsPopulated.put(fieldName, isFieldPopulated);

                            if (!shouldRedactFieldValue(fieldName) && isFieldPopulated) {
                                fieldValues.put(fieldName, fieldValue);
                            }
                        });

        return dataTrackerEventProducer.produceDataTrackerEvent(
                context.getRequest().getCredentials().getProviderName(),
                context.getCorrelationId(),
                fieldsPopulated,
                fieldValues,
                context.getAppId(),
                context.getClusterId(),
                context.getRequest().getCredentials().getUserId());
    }

    private boolean shouldRedactFieldValue(String fieldName) {
        // we do not want to redact Transaction<*>.transactionDate_*
        if (fieldName.startsWith("Transaction<") && fieldName.contains("transactionDate_")) {
            return false;
        }

        // we do not want to redact Transaction<*>.date/timestamp
        if (fieldName.startsWith("Transaction<")
                && ((fieldName.contains(".date") || fieldName.contains(".timestamp")))) {
            return false;
        }

        return true;
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
