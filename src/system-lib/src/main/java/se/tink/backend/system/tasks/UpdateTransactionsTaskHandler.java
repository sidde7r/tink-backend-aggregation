package se.tink.backend.system.tasks;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.tasks.interfaces.TaskHandler;
import se.tink.backend.system.rpc.Transaction;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;
import se.tink.backend.utils.LogUtils;

public class UpdateTransactionsTaskHandler implements TaskHandler<UpdateTransactionsTask> {
    private ServiceContext serviceContext;
    private Semaphore mainTopicSemaphore;

    private static final LogUtils log = new LogUtils(UpdateTransactionsTaskHandler.class);

    private Predicate<UpdateTransactionsTask> predicate = Predicates.alwaysTrue();

    public UpdateTransactionsTaskHandler(ServiceContext serviceContext, int mainTopicLimit) {
        this.serviceContext = serviceContext;
        this.mainTopicSemaphore = new Semaphore(mainTopicLimit);
    }

    @Override
    public void handle(Task<?> task) {
        Preconditions.checkArgument(task instanceof UpdateTransactionsTask);

        UpdateTransactionsTask updateTransactionsTask = (UpdateTransactionsTask) task;
        
        if (!predicate.apply(updateTransactionsTask)) {
            log.warn(String.format("Ignoring transaction update task because of negative predicate: %s",
                    updateTransactionsTask));
            return;
        }

        UpdateTransactionsRequest updateTransactionsRequest = updateTransactionsTask.getPayload();
        updateTransactionsRequest.setTopic(updateTransactionsTask.getTopic());
        if (serviceContext.getConfiguration().getTaskQueue().skipPending()) {
            List<Transaction> nonPendingTransactions = updateTransactionsRequest.getTransactions().stream()
                    .filter(t -> !t.isPending())
                    .collect(Collectors.toList());
            if (nonPendingTransactions.isEmpty()) {
                log.info("Skipping pending transactions update for user {}",
                        updateTransactionsRequest.getUser());
                return;
            }
            updateTransactionsRequest.setTransactions(nonPendingTransactions);
        }

        int transactionsSize = updateTransactionsRequest.getTransactions().size();
        String credentialsId = updateTransactionsRequest.getCredentials();
        String userId = updateTransactionsRequest.getUser();
        
        log.info(userId, credentialsId, String.format("TASK QUEUE: Consumed %s transactions.", transactionsSize));

        final TasksQueueConfiguration.Modes taskQueueMode = (serviceContext.getConfiguration().getTaskQueue() != null)
                    ? serviceContext.getConfiguration().getTaskQueue().getMode()
                    : TasksQueueConfiguration.DEFAULT_MODE;

        if (TasksQueueConfiguration.SHOULD_CONSUME.contains(taskQueueMode)) {
            if (Task.UPDATE_TRANSACTIONS_TOPIC.equals(task.getTopic())) {
                mainTopicSemaphore.acquireUninterruptibly();
                try {
                    serviceContext.getSystemServiceFactory().getProcessService()
                            .updateTransactionsSynchronously(updateTransactionsRequest);
                } finally {
                    mainTopicSemaphore.release();
                }
            } else {
                boolean semaphoreAcquired = mainTopicSemaphore.tryAcquire();
                try {
                    serviceContext.getSystemServiceFactory().getProcessService()
                            .updateTransactionsSynchronously(updateTransactionsRequest);
                } finally {
                    if (semaphoreAcquired) {
                        mainTopicSemaphore.release();
                    }
                }
            }
        }
    }

    @Override
    public Set<Class<? extends UpdateTransactionsTask>> handles() {
        return ImmutableSet.<Class<? extends UpdateTransactionsTask>> of(UpdateTransactionsTask.class);
    }

    public void setPredicate(Predicate<UpdateTransactionsTask> predicate) {
        this.predicate = predicate;
    }

}
