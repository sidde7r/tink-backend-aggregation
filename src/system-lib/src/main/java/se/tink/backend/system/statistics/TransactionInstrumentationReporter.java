package se.tink.backend.system.statistics;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.system.tasks.Task;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.backend.core.Transaction;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;

/**
 * Reporter class that is used to measure the time of the "whole chain" from when we received a transaction in the
 * connector until it is saved and statistics and activities are generated.
 * <p/>
 * It uses different timers for when we receive a single transaction and when we receive multiple. Multiple can be used
 * to measure how long time it the signup takes from when we get the first transaction until all in saved and processed.
 * <p/>
 */
public class TransactionInstrumentationReporter {

    private static final LogUtils log = new LogUtils(TransactionInstrumentationReporter.class);
    private static final ImmutableList<? extends Number> DELAY_BUCKETS = ImmutableList.of(0, 0.25, 0.5, 1,
            2, 4, 5, 10, 30, TimeUnit.MINUTES.toSeconds(1), TimeUnit.MINUTES.toSeconds(5),
            TimeUnit.MINUTES.toSeconds(15), TimeUnit.MINUTES.toSeconds(30),
            TimeUnit.HOURS.toSeconds(1), TimeUnit.HOURS.toSeconds(2),
            TimeUnit.HOURS.toSeconds(4), TimeUnit.HOURS.toSeconds(8),
            TimeUnit.HOURS.toSeconds(12), TimeUnit.DAYS.toSeconds(1),
            TimeUnit.DAYS.toSeconds(2));

    private final Timer externalDelaySingleTransactionTimer;
    private final Cluster cluster;
    private final MetricRegistry metricRegistry;

    @VisibleForTesting static final String PROCESS_TRANSACTIONS = "process_transactions";
    @VisibleForTesting static final String PRIORITY_LABEL = "priority";
    @VisibleForTesting static final String HIGH_PRIORITY_LABEL = "high";
    @VisibleForTesting static final String LOW_PRIORITY_LABEL = "low";
    @VisibleForTesting static final String EXTERNAL_DELAY_SINGLE_TRANSACTION = "external_delay_single_transaction";

    public TransactionInstrumentationReporter(ServiceConfiguration configuration, MetricRegistry metricRegistry) {
        externalDelaySingleTransactionTimer = metricRegistry.timer(MetricId.newId(EXTERNAL_DELAY_SINGLE_TRANSACTION), DELAY_BUCKETS);
        cluster = configuration.getCluster();
        this.metricRegistry = metricRegistry;
    }

    public void report(List<Transaction> transactions, String topic) {

        if (Objects.equals(cluster, Cluster.TINK)) {
            return;
        }

        if (transactions.isEmpty()) {
            return;
        }

        reportInternalProcessingTime(transactions, topic);

        reportExternalDelayTime(transactions);
    }

    /**
     * Report how long time it took to process the transaction on the Tink backend.
     */
    private void reportInternalProcessingTime(List<Transaction> transactions, String topic) {
        Transaction firstTransaction = getFirstTransaction(transactions);

        Long incomingTimestamp = getIncomingTimestamp(firstTransaction);

        if (incomingTimestamp == null) {
            return;
        }

        Timer timer = metricRegistry.timer(MetricId.newId(PROCESS_TRANSACTIONS).label(PRIORITY_LABEL,
                                Task.UPDATE_HIGH_PRIO_TRANSACTIONS_TOPIC.equals(topic)
                                ? HIGH_PRIORITY_LABEL
                                : LOW_PRIORITY_LABEL));
        long duration = new Date().getTime() - incomingTimestamp;
        timer.update(duration, TimeUnit.MILLISECONDS);

        String formattedDuration = DateUtils.prettyFormatMillis((int) duration);

        log.info(firstTransaction.getUserId(), firstTransaction.getCredentialsId(),
                String.format("Transactions(Count = '%d', Time = '%s')", transactions.size(), formattedDuration));
    }

    /**
     * Report how long time it took from when the transaction was executed (original date) until it was received in
     * the connector.
     */
    private void reportExternalDelayTime(List<Transaction> transactions) {
        if (transactions.size() != 1) {
            return;
        }

        Transaction transaction = transactions.get(0);

        Long incomingTimestamp = getIncomingTimestamp(transaction);

        if (incomingTimestamp == null) {
            return;
        }

        long duration = incomingTimestamp - transaction.getOriginalDate().getTime();

        externalDelaySingleTransactionTimer.update(duration, TimeUnit.MILLISECONDS);

        String formattedDuration = DateUtils.prettyFormatMillis((int) duration);

        log.info(transaction.getUserId(), transaction.getCredentialsId(),
                String.format("External Delay(Time = '%s')", formattedDuration));
    }

    @VisibleForTesting
    static Long getIncomingTimestamp(Transaction transaction) {

        if (transaction == null) {
            return null;
        }

        String incomingTimestamp = transaction.getInternalPayload(Transaction.InternalPayloadKeys.INCOMING_TIMESTAMP);

        if (Strings.isNullOrEmpty(incomingTimestamp)) {
            return null;
        }

        return Long.parseLong(incomingTimestamp);
    }

    @VisibleForTesting
    static Transaction getFirstTransaction(List<Transaction> transactions) {

        if (transactions == null || transactions.isEmpty()) {
            return null;
        }

        Transaction minTransaction = transactions.get(0);
        Long minTimestamp = getIncomingTimestamp(minTransaction);

        for (int i = 1; i < transactions.size(); i++) {
            Transaction transaction = transactions.get(i);

            Long timestamp = getIncomingTimestamp(transaction);

            if (timestamp != null && (minTimestamp == null || timestamp < minTimestamp)) {
                minTransaction = transaction;
                minTimestamp = timestamp;
            }

        }

        return minTransaction;
    }
}
