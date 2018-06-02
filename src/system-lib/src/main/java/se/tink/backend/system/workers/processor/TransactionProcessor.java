package se.tink.backend.system.workers.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.time.StopWatch;
import se.tink.backend.categorization.rules.AbnAmroIcsCategorizationCommand;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.core.CategoryChangeRecord;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.core.UserData;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.system.workers.processor.categorization.UnknownCategorizationCommand;
import se.tink.backend.system.workers.processor.chaining.ChainFactory;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.jersey.logging.UserRuntimeException;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;

public class TransactionProcessor {
    private static final LogUtils log = new LogUtils(TransactionProcessor.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static class MetricKey {
        public static final String PROVIDER = "provider";
        public static final String TYPE = "type";
    }

    private static final MetricId METRIC_ID = MetricId.newId("transaction_processor");
    public static final MetricId UNCHANGED_TRANSACTIONS_METRIC_ID = TransactionProcessor.METRIC_ID
            .label(MetricKey.TYPE, "unchanged");
    public static final MetricId UPDATED_TRANSACTIONS_METRIC_ID = TransactionProcessor.METRIC_ID
            .label(MetricKey.TYPE, "updated");
    public static final MetricId NEW_TRANSACTIONS_METRIC_ID = TransactionProcessor.METRIC_ID
            .label(MetricKey.TYPE, "new");
    public static final MetricId DELETED_TRANSACTIONS_METRIC_ID = TransactionProcessor.METRIC_ID
            .label(MetricKey.TYPE, "deleted");

    public static class MetricBuckets {
        public static final List<Integer> X_LARGE = ImmutableList.of(0, 25, 50, 100, 300);
        public static final List<Integer> LARGE = ImmutableList.of(0, 5, 10, 20, 50, 100, 300);
        public static final List<Integer> MEDIUM = ImmutableList.of(0, 2, 4, 8, 16, 32);
        public static final List<Integer> SMALL = ImmutableList.of(0, 1, 2, 4, 8, 16, 32);
    }

    private static final MetricId TRANSACTIONS_INCOMING = MetricId.newId("transactions_incoming");
    public static final MetricId TRANSACTIONS_INCOMING_NEGATIVE = TRANSACTIONS_INCOMING.label("sign", "negative");
    public static final MetricId TRANSACTIONS_INCOMING_POSITIVE = TRANSACTIONS_INCOMING.label("sign", "positive");
    public static final MetricId TRANSACTIONS_NEW = MetricId.newId("transactions_new");
    private static final MetricId PROCESS_TRANSACTIONS = MetricId.newId("process_transactions");
    public static final MetricId TRANSACTIONS_NO_NEW = MetricId.newId("transactions_no_new");

    private final Counter positiveIncomingTransactionMeter;
    private final Counter negativeIncomingTransactionMeter;
    private final Counter newTransactionMeter;
    private final Counter noNewTransactionsMeter;

    private MetricRegistry metricRegistry;

    // TODO: Make private. See https://github.com/google/guice/wiki/KeepConstructorsHidden.
    @Inject
    public TransactionProcessor(MetricRegistry metricRegistry) {

        this.metricRegistry = metricRegistry;

        this.positiveIncomingTransactionMeter = metricRegistry.meter(TRANSACTIONS_INCOMING_POSITIVE);
        this.negativeIncomingTransactionMeter = metricRegistry.meter(TRANSACTIONS_INCOMING_NEGATIVE);
        this.noNewTransactionsMeter = metricRegistry.meter(TRANSACTIONS_NO_NEW);
        this.newTransactionMeter = metricRegistry.meter(TRANSACTIONS_NEW);
    }

    public void warmUp(ServiceContext context) {
        if (Objects.equal(Cluster.ABNAMRO, context.getConfiguration().getCluster())) {
            Stopwatch timer = Stopwatch.createStarted();
            AbnAmroIcsCategorizationCommand.warmUp();
            log.debug(String.format(
                    "Warmed up AbnAmroIcsCategorizationCommand in %d ms.",
                    timer.elapsed(TimeUnit.MILLISECONDS)
            ));
        }
    }

    private enum TransactionsProcessorMetricType {
        INITIALIZE_COMMAND("initialize-command"),
        EXECUTE_COMMAND("execute-command"),
        POST_PROCESS_COMMAND("post-process-command");

        private String stringRepresentation;

        private TransactionsProcessorMetricType(String stringReprentation) {
            this.stringRepresentation = stringReprentation;
        }

        @Override
        public String toString() {
            return stringRepresentation;
        }
    }

    /**
     * Create a TimerContext for a specific command.
     *
     * @param commandClass Class of command.
     * @return The timer context.
     */
    private Timer.Context getTimerContext(
            Class<? extends TransactionProcessorCommand> commandClass,
            TransactionsProcessorMetricType type
    ) {
        return metricRegistry.timer(MetricId.newId("transaction_command_duration")
                .label("class", commandClass.getSimpleName())
                .label("command", type.toString())).time();
    }

    /**
     * Processes a batch of transactions with the specified command chains. If any of the commands return BREAK, nothing
     * more happens. If the processor reaches the end of the chain or a command returns CONTINUE, the finish command is
     * executed.
     * <p>
     * !!!!!!!!!!!!!!!!
     * NB! Provider dependent commands will not be initialized when running this, since `UserData#credentials`
     * and `TransactionProcessorContext#credentialsId` are not set.
     * !!!!!!!!!!!!!!!!
     */
    @VisibleForTesting
    public void processTransactions(
            User user, List<Transaction> transactions, ChainFactory commandChain,
            ServiceContext serviceContext
    ) {

        TransactionProcessorContext context = new TransactionProcessorContext(
                user,
                serviceContext.getDao(ProviderDao.class).getProvidersByName(),
                transactions
        );
        UserData userData = new UserData();
        userData.setUser(user);
        userData.setTransactions(transactions);

        processTransactions(context, commandChain, userData, false);
    }

    /**
     * Processes a batch of transactions with the specified command chains. If any of the commands return BREAK, nothing
     * more happens. If the processor reaches the end of the chain or a command returns CONTINUE, the finish command is
     * executed.
     */
    public void processTransactions(
            TransactionProcessorContext context, ChainFactory commandChain, UserData userData,
            final boolean rethrowAllExceptions
    ) {
        String userId = userData.getUser().getId();

        final ImmutableList<TransactionProcessorCommand> commands = commandChain.build(context);

        // Start a processing timer.

        Timer.Context timerContext = metricRegistry.timer(PROCESS_TRANSACTIONS).time();

        StopWatch watch = new StopWatch();
        watch.start();

        log.info(userId, "Running transaction processing chain.");

        // Initialize the command chain.

        log.debug(userId, "Executing initialize commands");

        for (TransactionProcessorCommand command : commands) {
            log.debug(userId, "\t" + command);

            TransactionProcessorCommandResult result;

            final Context commandTimerContext = getTimerContext(
                    command.getClass(),
                    TransactionsProcessorMetricType.INITIALIZE_COMMAND
            );

            try {
                result = command.initialize();
            } catch (Exception e) {

                result = TransactionProcessorCommandResult.BREAK;

                String message = String.format("Could not run initialize for command: %s", command);

                if (rethrowAllExceptions) {
                    throw new UserRuntimeException(userId, message, e);
                } else {
                    log.error(userId, message, e);
                }
            }

            commandTimerContext.stop();

            switch (result) {
            case BREAK:
                watch.stop();
                timerContext.stop();

                instrumentIncomingTransactions(context.getInBatchTransactions());
                noNewTransactionsMeter.inc();

                log.info(userId, "Processed transaction in " + watch.toString());
                return;
            default:
                continue;
            }
        }

        // Execute the command chain.

        log.debug(userId, "Processing transactions.");

        boolean foundNewTransactions = false;

        transactionLoop:
        for (Transaction t : context.getInBatchTransactions()) {
            final Transaction transaction = t;

            if (log.isTraceEnabled()) {
                // Avoid serialization of transaction unless necessary.

                try {
                    log.trace(userId, t.getCredentialsId(), "Processing: " + mapper.writeValueAsString(transaction));
                } catch (Exception e) {
                    // NOOP.
                    if (rethrowAllExceptions) {
                        throw new RuntimeException(e);
                    }
                }
            }

            commandLoop:
            for (TransactionProcessorCommand command : commands) {
                log.trace(userId, t.getCredentialsId(), "\t\tExecuting: " + command);

                String categoryBefore = transaction.getCategoryId();

                // Time the command execution.

                final Context commandTimerContext = getTimerContext(
                        command.getClass(),
                        TransactionsProcessorMetricType.EXECUTE_COMMAND
                );

                // Execute the command.

                TransactionProcessorCommandResult result = TransactionProcessorCommandResult.CONTINUE;

                try {
                    result = command.execute(transaction);
                } catch (Exception e) {
                    String message = String.format("Could not run execute for command: %s", command);

                    if (rethrowAllExceptions) {
                        throw new UserRuntimeException(userId, message, e);
                    } else {
                        log.error(userId, message, e);
                    }
                }

                commandTimerContext.stop();

                // Keep track of categorization changes.

                String categoryAfter = transaction.getCategoryId();

                if (ObjectUtils.notEqual(categoryBefore, categoryAfter)
                        && ObjectUtils.notEqual(command.getClass(), UnknownCategorizationCommand.class)) {

                    context.addCategoryChangeRecord(CategoryChangeRecord.createChangeRecord(
                            transaction, Optional.ofNullable(categoryBefore), command.toString()));
                }

                if (log.isTraceEnabled()) {
                    // Avoid serialization if necessary.

                    try {
                        log.trace(userId, t.getCredentialsId(),
                                "\t\t\tTransaction: " + mapper.writeValueAsString(transaction)
                        );
                    } catch (Exception e) {
                        // NOOP.
                        if (rethrowAllExceptions) {
                            throw new RuntimeException(e);
                        }
                    }

                    log.trace(userId, t.getCredentialsId(), "\t\t\tResult: " + result.name());
                }

                switch (result) {
                case BREAK:
                    continue transactionLoop;
                case FINISH:
                    break commandLoop;
                default:
                    continue commandLoop;
                }
            }

            newTransactionMeter.inc();
            foundNewTransactions = true;
        }

        if (!foundNewTransactions) {
            noNewTransactionsMeter.inc();
        }

        log.debug(userId, "Executing post process commands");

        // Finalize the command chain.

        for (TransactionProcessorCommand command : commands) {
            log.debug(userId, "\t" + command);

            final Context commandTimerContext = getTimerContext(
                    command.getClass(),
                    TransactionsProcessorMetricType.POST_PROCESS_COMMAND
            );

            try {
                command.postProcess();
            } catch (Exception e) {
                String message = String.format("Could not run postProcess for processing command: %s", command);

                if (rethrowAllExceptions) {
                    throw new UserRuntimeException(userId, message, e);
                } else {
                    log.error(userId, message, e);
                }
            }

            commandTimerContext.stop();
        }

        // Done processing.

        watch.stop();
        timerContext.stop();

        instrumentIncomingTransactions(context.getInBatchTransactions());

        log.info(userId, "Processed full transaction chain in " + watch.toString());
    }

    private void instrumentIncomingTransactions(final List<Transaction> inBatchTransactions) {
        final long numberOfTxsWithPositiveAmount = inBatchTransactions.stream().filter(t -> t.getAmount() >= 0).count();
        positiveIncomingTransactionMeter.inc(numberOfTxsWithPositiveAmount);

        final long numberofTxzWithNegativeAmount = inBatchTransactions.size() - numberOfTxsWithPositiveAmount;
        negativeIncomingTransactionMeter.inc(numberofTxzWithNegativeAmount);
    }
}
