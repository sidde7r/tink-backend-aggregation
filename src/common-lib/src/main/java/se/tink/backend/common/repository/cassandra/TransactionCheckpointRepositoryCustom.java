package se.tink.backend.common.repository.cassandra;

import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.CheckpointTransaction;

public interface TransactionCheckpointRepositoryCustom extends Creatable {
    void saveQuicklyWithTTL(CheckpointTransaction transactionSnapshot, long ttl, TimeUnit ttlUnit);

    List<CheckpointTransaction> findByUserIdAndCheckpointId(String userid, String checkpointid);
}
