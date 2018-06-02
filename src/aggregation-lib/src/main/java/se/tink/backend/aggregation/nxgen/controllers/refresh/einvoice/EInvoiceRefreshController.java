package se.tink.backend.aggregation.nxgen.controllers.refresh.einvoice;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.Refresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.core.transfer.Transfer;
import se.tink.libraries.metrics.MetricId;

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

    public void refreshEInvoices() {
        MetricRefreshAction action = metricRefreshController.buildAction(METRIC_ID, COUNTER_METRIC_BUCKETS);

        try {
            action.start();

            List<Transfer> eInvoices = getEInvoices();
            updateController.updateEInvoices(eInvoices);

            action.count(eInvoices.size());
            action.completed();
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }

    public List<Transfer> getEInvoices() {
        if (eInvoiceFetcher == null) {
            return Collections.emptyList();
        }
        Collection<Transfer> eInvoices = eInvoiceFetcher.fetchEInvoices();
        return Lists.newArrayList(eInvoices != null ? eInvoices : Collections.emptyList());
    }
}
