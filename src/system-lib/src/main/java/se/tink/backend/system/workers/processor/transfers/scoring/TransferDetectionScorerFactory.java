package se.tink.backend.system.workers.processor.transfers.scoring;

import java.util.List;
import se.tink.backend.core.Account;
import se.tink.libraries.cluster.Cluster;

public interface TransferDetectionScorerFactory {
    TransferDetectionScorer build(List<Account> accounts);

    // TODO: Migrate this to cluster specific Guice module.
    static TransferDetectionScorerFactory byCluster(Cluster cluster) {
        switch (cluster) {
        case ABNAMRO:
            return a -> new AbnAmroTransferDetectionScorer(a);
        default:
            return a -> new DefaultTransferDetectionScorer();
        }
    }
}
