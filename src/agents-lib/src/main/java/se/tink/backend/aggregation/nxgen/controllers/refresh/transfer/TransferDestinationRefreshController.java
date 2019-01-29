package se.tink.backend.aggregation.nxgen.controllers.refresh.transfer;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.aggregation.agents.models.TransferDestinationPattern;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshAction;
import se.tink.backend.aggregation.nxgen.controllers.metrics.MetricRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.Refresher;
import se.tink.libraries.metrics.MetricId;

import java.util.*;

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
    private final TransferDestinationFetcher transferDestinationFetcher;

    public TransferDestinationRefreshController(MetricRefreshController metricRefreshController,
             TransferDestinationFetcher transferDestinationFetcher) {
        this.metricRefreshController = Preconditions.checkNotNull(metricRefreshController);
        this.transferDestinationFetcher = Preconditions.checkNotNull(transferDestinationFetcher);
    }

    public Map<Account, List<TransferDestinationPattern>> refreshTransferDestinationsFor(Collection<Account> accounts) {
        MetricRefreshAction action = metricRefreshController.buildAction(METRIC_ID, COUNTER_METRIC_BUCKETS);

        try {
            action.start();
            Map<Account, List<TransferDestinationPattern>> transferDestinations = new HashMap<>();

            Optional.ofNullable(transferDestinationFetcher.fetchTransferDestinationsFor(accounts))
                    .ifPresent(destinationsResponse -> {

                        transferDestinations.putAll(destinationsResponse.getDestinations());
                        destinationsResponse.getDestinations().values()
                                .forEach(destinations -> action.count(destinations.size()));
                    });

            action.completed();
            return transferDestinations;
        } catch (RuntimeException e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }
}
