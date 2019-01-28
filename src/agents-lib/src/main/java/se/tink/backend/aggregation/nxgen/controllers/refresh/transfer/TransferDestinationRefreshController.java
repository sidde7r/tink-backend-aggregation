package se.tink.backend.aggregation.nxgen.controllers.refresh.transfer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.Refresher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.UpdateController;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.metrics.MetricId;

public final class TransferDestinationRefreshController implements Refresher {
    private static final MetricId METRIC_ID = Refresher.REFRESHER_METRIC_ID
            .label(Refresher.METRIC_ITEM_TYPE, "transfer_destinations");
    private static final List<Number> COUNTER_METRIC_BUCKETS = ImmutableList.<Number>builder()
            .add(0)
            .add(1)
            .add(2)
            .add(3)
            .add(5)
            .add(8)
            .add(13)
            .add(21)
            .add(34)
            .add(55)
            .add(89)
            .build();

    private final MetricRefreshController metricRefreshController;
    private final UpdateController updateController;
    private final TransferDestinationFetcher transferDestinationFetcher;

    public TransferDestinationRefreshController(MetricRefreshController metricRefreshController,
            UpdateController updateController, TransferDestinationFetcher transferDestinationFetcher) {
        this.metricRefreshController = Preconditions.checkNotNull(metricRefreshController);
        this.updateController = Preconditions.checkNotNull(updateController);
        this.transferDestinationFetcher = Preconditions.checkNotNull(transferDestinationFetcher);
    }

    public void refreshTransferDestinationsFor(Collection<Account> accounts) {
        MetricRefreshAction action = metricRefreshController.buildAction(METRIC_ID, COUNTER_METRIC_BUCKETS);

        try {
            action.start();

            Optional.ofNullable(transferDestinationFetcher.fetchTransferDestinationsFor(accounts))
                    .ifPresent(destinationsResponse -> {
                        updateController.updateTransferDestinationPatterns(destinationsResponse);
                        destinationsResponse.getDestinations().values()
                                .forEach(destinations -> action.count(destinations.size()));
                    });

            action.completed();
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }
}
