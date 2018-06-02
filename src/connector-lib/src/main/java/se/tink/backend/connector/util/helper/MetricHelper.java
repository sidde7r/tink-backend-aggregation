package se.tink.backend.connector.util.helper;

import com.google.inject.Inject;
import java.util.List;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.core.Transaction;

public class MetricHelper {

    private static final String CONNECTOR_TRANSACTIONS_INCOMING = "connector_transactions_incoming";

    private MetricRegistry metricRegistry;

    @Inject
    public MetricHelper(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public void reportTransactionMetric(List<Transaction> transactions, TransactionContainerType containerType) {
        metricRegistry.meter(MetricId.newId(CONNECTOR_TRANSACTIONS_INCOMING).label("origin", containerType.toString()))
                .inc(transactions.size());
    }
}
