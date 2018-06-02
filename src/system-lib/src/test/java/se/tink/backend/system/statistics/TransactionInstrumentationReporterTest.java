package se.tink.backend.system.statistics;

import com.google.common.collect.Lists;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.core.Transaction.InternalPayloadKeys;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.tasks.Task;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class TransactionInstrumentationReporterTest {
    TransactionInstrumentationReporter reporter;
    MetricRegistry metricRegistry;

    @Before
    public void setUp() {
        metricRegistry = new MetricRegistry();
        reporter = new TransactionInstrumentationReporter(new ServiceConfiguration(),
                metricRegistry);
    }

    @Test
    @Parameters({Task.UPDATE_TRANSACTIONS_TOPIC + ","
                    + TransactionInstrumentationReporter.LOW_PRIORITY_LABEL,
                 Task.UPDATE_HIGH_PRIO_TRANSACTIONS_TOPIC + ","
                    + TransactionInstrumentationReporter.HIGH_PRIORITY_LABEL})
    public void reportForSingleTransaction(String topic, String priority) {
        Transaction transaction = new Transaction();
        transaction.setInternalPayload(Transaction.InternalPayloadKeys.INCOMING_TIMESTAMP, "0");
        transaction.setOriginalDate(new Date());

        reporter.report(singletonList(transaction), topic);

        Timer internalDelayTimer = metricRegistry.timer(MetricId.newId(
                    TransactionInstrumentationReporter.PROCESS_TRANSACTIONS)
                        .label(TransactionInstrumentationReporter.PRIORITY_LABEL, priority));
        assertEquals(1, internalDelayTimer.getCount());
        Timer externalDelayTimer = metricRegistry.timer(MetricId.newId(
                    TransactionInstrumentationReporter.EXTERNAL_DELAY_SINGLE_TRANSACTION));
        assertEquals(1, externalDelayTimer.getCount());
    }

    @Test
    @Parameters({Task.UPDATE_TRANSACTIONS_TOPIC + ","
                    + TransactionInstrumentationReporter.LOW_PRIORITY_LABEL,
                 Task.UPDATE_HIGH_PRIO_TRANSACTIONS_TOPIC + ","
                    + TransactionInstrumentationReporter.HIGH_PRIORITY_LABEL})
    public void reportForMultipleHighPrioTransactions(String topic, String priority) {
        Transaction transaction1 = new Transaction();
        transaction1.setInternalPayload(Transaction.InternalPayloadKeys.INCOMING_TIMESTAMP, "1");
        transaction1.setOriginalDate(new Date());
        Transaction transaction2 = new Transaction();
        transaction2.setInternalPayload(Transaction.InternalPayloadKeys.INCOMING_TIMESTAMP, "2");

        reporter.report(asList(transaction1, transaction2), topic);

        Timer internalDelayTimer = metricRegistry.timer(MetricId.newId(
                    TransactionInstrumentationReporter.PROCESS_TRANSACTIONS)
                        .label(TransactionInstrumentationReporter.PRIORITY_LABEL, priority));
        assertEquals(1, internalDelayTimer.getCount());
        Timer externalDelayTimer = metricRegistry.timer(MetricId.newId(
                    TransactionInstrumentationReporter.EXTERNAL_DELAY_SINGLE_TRANSACTION));
        assertEquals(0, externalDelayTimer.getCount());
    }

    @Test
    public void nullIncomingTimestampShouldBeNull() {
        assertThat(TransactionInstrumentationReporter.getIncomingTimestamp(null)).isNull();
    }

    @Test
    public void emptyIncomingTimestampShouldBeNull() {
        Transaction transaction = new Transaction();

        assertThat(TransactionInstrumentationReporter.getIncomingTimestamp(transaction)).isNull();
    }

    @Test
    public void incomingTimestampShouldNotBeNull() {
        Date now = new Date();

        Transaction transaction = new Transaction();
        transaction.setInternalPayload(InternalPayloadKeys.INCOMING_TIMESTAMP, String.valueOf(now.getTime()));

        assertThat(TransactionInstrumentationReporter.getIncomingTimestamp(transaction)).isEqualTo(now.getTime());
    }

    @Test
    public void firstTransactionWithNullInputShouldBeNull() {
        assertThat(TransactionInstrumentationReporter.getFirstTransaction(null)).isNull();
    }

    @Test
    public void firstTransactionWithoutTransactionInputShouldBeNull() {

        List<Transaction> transactions = Lists.newArrayList();

        assertThat(TransactionInstrumentationReporter.getFirstTransaction(transactions)).isNull();
    }

    @Test
    public void transactionWithTimestampShouldBePicked() {
        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();

        Date now = new Date();

        t1.setInternalPayload(InternalPayloadKeys.INCOMING_TIMESTAMP, String.valueOf(now.getTime()));

        List<Transaction> transactions = Lists.newArrayList(t1, t2);

        assertThat(TransactionInstrumentationReporter.getFirstTransaction(transactions)).isEqualTo(t1);
    }

    @Test
    public void firstTransactionWithEarliestTimestampShouldBePicked() {
        Transaction t1 = new Transaction();
        Transaction t2 = new Transaction();

        Date early = new DateTime().minusSeconds(10).toDate();
        Date late = new DateTime().minusSeconds(5).toDate();

        t1.setInternalPayload(InternalPayloadKeys.INCOMING_TIMESTAMP, String.valueOf(early.getTime()));
        t2.setInternalPayload(InternalPayloadKeys.INCOMING_TIMESTAMP, String.valueOf(late.getTime()));

        List<Transaction> transactions = Lists.newArrayList(t2, t1);

        assertThat(TransactionInstrumentationReporter.getFirstTransaction(transactions)).isEqualTo(t1);
    }

}
