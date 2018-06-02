package se.tink.backend.system.workers.processor.transfers.scoring;

import se.tink.backend.core.Transaction;

public interface TransferDetectionScorer {
    double getScore(Transaction left, Transaction right);
}
