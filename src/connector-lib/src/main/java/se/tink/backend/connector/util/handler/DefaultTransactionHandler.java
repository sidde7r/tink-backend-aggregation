package se.tink.backend.connector.util.handler;

import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import se.tink.backend.common.config.TasksQueueConfiguration;
import se.tink.backend.common.tasks.interfaces.TaskSubmitter;
import se.tink.backend.connector.rpc.CreateTransactionEntity;
import se.tink.backend.connector.rpc.PartnerTransactionPayload;
import se.tink.backend.connector.rpc.TransactionContainerType;
import se.tink.backend.connector.utils.TagValidationUtils;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.DeleteTransactionRequest;
import se.tink.backend.system.rpc.TransactionToDelete;
import se.tink.backend.system.tasks.DeleteTransactionTask;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class DefaultTransactionHandler implements TransactionHandler {

    private TaskSubmitter taskSubmitter;
    private SystemServiceFactory systemServiceFactory;
    private TransactionIngestionHandler transactionIngestionHandler;
    private final MetricRegistry metricRegistry;
    private static final LogUtils log = new LogUtils(DefaultTransactionHandler.class);
    private static final MetricId TRANSACTION_TOPIC_METRIC = MetricId.newId("tink_transaction_topic");

    @Inject
    public DefaultTransactionHandler(TasksQueueConfiguration tasksQueueConfiguration, TaskSubmitter taskSubmitter,
            SystemServiceFactory systemServiceFactory, MetricRegistry metricRegistry) {
        this.taskSubmitter = taskSubmitter;
        this.systemServiceFactory = systemServiceFactory;
        this.transactionIngestionHandler = new TransactionIngestionHandler(tasksQueueConfiguration, taskSubmitter);
        this.metricRegistry = metricRegistry;
    }

    @Override
    public Transaction mapToTinkModel(User user, Credentials credentials, Account account,
            CreateTransactionEntity transactionEntity) {

        Transaction transaction = new Transaction();

        transaction.setId(UUIDUtils.generateUUID());

        transaction.setAccountId(account.getId());
        transaction.setCredentialsId(credentials.getId());
        transaction.setUserId(user.getId());

        transaction.setOriginalAmount(transactionEntity.getAmount());
        transaction.setAmount(transactionEntity.getAmount());
        transaction.setDate(transactionEntity.getDate());
        transaction.setDescription(transactionEntity.getDescription());
        transaction.setType(transactionEntity.getType());
        transaction.setPending(transactionEntity.isPending());

        transaction.setPayload(TransactionPayloadTypes.EXTERNAL_ID, transactionEntity.getExternalId());

        transaction.setInternalPayload(Transaction.InternalPayloadKeys.INCOMING_TIMESTAMP,
                String.valueOf(transactionEntity.getEntityCreated().getTime()));

        String serializedPayload = SerializationUtils.serializeToString(transactionEntity.getPayload());
        transaction.setInternalPayload(Transaction.InternalPayloadKeys.PARTNER_PAYLOAD, serializedPayload);

        setTagsFromPayloadToTinkModel(transaction, serializedPayload);

        return transaction;
    }

    @Override
    public Transaction mapUpdateToTinkModel(Transaction transaction, CreateTransactionEntity updatedTransactionEntity) {
        // If user hasn't modified the amount/date/description, set the user modified value as well as setting
        // original value
        if (!transaction.isUserModifiedAmount()) {
            transaction.setAmount(updatedTransactionEntity.getAmount());
        }
        if (!transaction.isUserModifiedDate()) {
            transaction.setDate(updatedTransactionEntity.getDate());
        }
        if (!transaction.isUserModifiedDescription()) {
            transaction.setDescription(updatedTransactionEntity.getDescription());
        }

        transaction.setOriginalAmount(updatedTransactionEntity.getAmount());
        transaction.setOriginalDate(updatedTransactionEntity.getDate());
        transaction.setOriginalDescription(updatedTransactionEntity.getDescription());
        transaction.setType(updatedTransactionEntity.getType());
        transaction.setPending(updatedTransactionEntity.isPending());

        String serializedPayload = SerializationUtils.serializeToString(updatedTransactionEntity.getPayload());
        transaction.setInternalPayload(Transaction.InternalPayloadKeys.PARTNER_PAYLOAD, serializedPayload);

        setTagsFromPayloadToTinkModel(transaction, serializedPayload);

        return transaction;
    }

    @Override
    public void updateTransaction(Transaction transaction) {
        systemServiceFactory.getUpdateService().updateTransaction(transaction);
    }

    @Override
    public void ingestTransactions(User user, Credentials credentials, List<Transaction> transactions,
            TransactionContainerType type) throws ExecutionException {

        boolean userTriggered = Objects.equals(TransactionContainerType.HISTORICAL, type);
        boolean realTime = (type == TransactionContainerType.REAL_TIME);
        metricRegistry.meter(TRANSACTION_TOPIC_METRIC.label("type", type.name())).inc();
        transactionIngestionHandler.ingestTransactions(user.getId(), credentials, userTriggered, realTime, transactions);
    }

    @Override
    public void deleteTransactions(List<TransactionToDelete> transactionsToDelete, User user) {
        DeleteTransactionRequest request = new DeleteTransactionRequest();
        request.setUserId(user.getId());
        request.setTransactions(transactionsToDelete);

        DeleteTransactionTask task = new DeleteTransactionTask();
        task.setPayload(request);
        task.setPartitionKey(user.getId());

        log.info(user.getId(), String.format("TASK QUEUE - Submit delete transactions: %s.",
                SerializationUtils.serializeToString(transactionsToDelete)));

        try {
            Uninterruptibles.getUninterruptibly(taskSubmitter.submit(task));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void setTagsFromPayloadToTinkModel(Transaction transaction, String serializedPayload) {
        PartnerTransactionPayload partnerTransactionPayload =
                SerializationUtils.deserializeFromString(serializedPayload, PartnerTransactionPayload.class);

        if (partnerTransactionPayload != null) {
            List<String> tags = TagValidationUtils.normalizeTagList(partnerTransactionPayload.getTags());
            transaction.setNotes(String.join(" ", tags));
        }
    }
}
