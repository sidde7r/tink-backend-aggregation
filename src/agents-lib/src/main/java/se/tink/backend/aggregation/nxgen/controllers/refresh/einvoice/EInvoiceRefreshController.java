package se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.Refresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.libraries.transfer.rpc.Transfer;
import se.tink.libraries.metrics.MetricId;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class EInvoiceRefreshController implements Refresher {
    private static final MetricId METRIC_ID = Refresher.REFRESHER_METRIC_ID.label(Refresher.METRIC_ITEM_TYPE, "einvoices");
    private static final List<Number> COUNTER_METRIC_BUCKETS = ImmutableList.<Number>builder()
            .add(0)
            .add(1)
            .add(2)
            .add(3)
            .add(5)
            .add(8)
            .add(13)
            .add(21)
            .build();

    private final MetricRefreshController metricRefreshController;
    private final UpdateController updateController;
    private final EInvoiceFetcher eInvoiceFetcher;

    public EInvoiceRefreshController(MetricRefreshController metricRefreshController, UpdateController updateController,
            EInvoiceFetcher eInvoiceFetcher) {
        this.metricRefreshController = Preconditions.checkNotNull(metricRefreshController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.eInvoiceFetcher = Preconditions.checkNotNull(eInvoiceFetcher);
    }

    public List<Transfer> refreshEInvoices() {
        MetricRefreshAction action = metricRefreshController.buildAction(METRIC_ID, COUNTER_METRIC_BUCKETS);

        try {
            action.start();

            if (eInvoiceFetcher == null) {
                return Collections.emptyList();
            }
            Collection<Transfer> eInvoices = eInvoiceFetcher.fetchEInvoices();


            action.count(eInvoices.size());
            action.completed();

            return Lists.newArrayList(eInvoices);
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }
}
