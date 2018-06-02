package se.tink.backend.connector.util.handler;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.mapper.CoreTransactionMapper;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;
import se.tink.backend.system.tasks.Task;
import se.tink.backend.system.tasks.UpdateTransactionsTask;
import se.tink.backend.utils.LogUtils;

public class TransactionIngestionHandler {
    private final TasksQueueConfiguration tasksQueueConfiguration;
    private final TaskSubmitter taskSubmitter;
    private static final LogUtils log = new LogUtils(TransactionIngestionHandler.class);

    @Inject
    public TransactionIngestionHandler(TasksQueueConfiguration tasksQueueConfiguration, TaskSubmitter taskSubmitter) {
        this.tasksQueueConfiguration = tasksQueueConfiguration;
        this.taskSubmitter = taskSubmitter;
    }

    public void ingestTransactions(String userId, Credentials credentials, boolean userTriggered, boolean realTime,
            List<Transaction> transactions)
            throws ExecutionException {
        UpdateTransactionsRequest request = new UpdateTransactionsRequest();

        request.setUser(userId);
        request.setCredentials(credentials.getId());
        request.setUserTriggered(userTriggered);
        request.setTransactions(CoreTransactionMapper.toSystemTransaction(transactions));

        Task<UpdateTransactionsRequest> task = (realTime && tasksQueueConfiguration.useHighPrioTopic()) ?
                new UpdateTransactionsTask(Task.UPDATE_HIGH_PRIO_TRANSACTIONS_TOPIC) :
                new UpdateTransactionsTask(Task.UPDATE_TRANSACTIONS_TOPIC);
        task.setPayload(request);
        task.setPartitionKey(credentials.getUserId());

        log.info(credentials, String.format("TASK QUEUE: Submit %s transactions.", transactions.size()));

        Uninterruptibles.getUninterruptibly(taskSubmitter.submit(task));
    }
}
