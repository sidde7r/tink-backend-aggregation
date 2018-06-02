package se.tink.backend.system.tasks;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.repository.cassandra.TransactionCheckpointRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.tasks.interfaces.TaskHandler;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.CheckpointTransaction;
import se.tink.backend.system.rpc.CheckpointRollbackRequest;
import se.tink.backend.system.tasks.helper.DeleteTransactionsHelper;

public class CheckpointRollbackTaskHandler implements TaskHandler<CheckpointRollbackTask> {

    private final TransactionCheckpointRepository transactionCheckpointRepository;
    private final DeleteTransactionsHelper deleteTransactionsHelper;

    public CheckpointRollbackTaskHandler(ServiceContext serviceContext) {
        this.transactionCheckpointRepository = serviceContext.getRepository(TransactionCheckpointRepository.class);

        deleteTransactionsHelper = new DeleteTransactionsHelper(
                serviceContext,
                serviceContext.getCoordinationClient(),
                serviceContext.getRepository(UserStateRepository.class));
    }

    @Override
    public Set<Class<? extends CheckpointRollbackTask>> handles() {
        return ImmutableSet.<Class<? extends CheckpointRollbackTask>> of(CheckpointRollbackTask.class);
    }

    @Override
    public void handle(Task<?> deserializedPayload) {
        Preconditions.checkArgument(deserializedPayload instanceof CheckpointRollbackTask);

        CheckpointRollbackTask checkpointRollbackTask = (CheckpointRollbackTask) deserializedPayload;
        CheckpointRollbackRequest checkpointRollbackRequest = checkpointRollbackTask.getPayload();

        String userId = checkpointRollbackRequest.getUserId();

        List<CheckpointTransaction> transactions = transactionCheckpointRepository
                .findByUserIdAndCheckpointId(userId, checkpointRollbackRequest.getCheckpointId());

        // Do the transaction cleanup.

        List<String> transactionIdsToRemove = transactions.stream()
                .map(t -> UUIDUtils.toTinkUUID(t.getTransactionId()))
                .collect(Collectors.toList());

        deleteTransactionsHelper.delete(userId, transactionIdsToRemove);
    }
}
