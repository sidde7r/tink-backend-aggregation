package se.tink.backend.system.tasks;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.TransactionExternalIdRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.tasks.interfaces.TaskHandler;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionExternalId;
import se.tink.backend.core.TransactionPayloadTypes;
import se.tink.backend.system.rpc.DeleteTransactionRequest;
import se.tink.backend.system.tasks.helper.DeleteTransactionsHelper;
import se.tink.backend.utils.LogUtils;

/**
 * Currently after transaction deletion, the user's statistics are removed.
 * Our main api will notice that no statistics exists on next statistics query and
 * will trigger a recalculation while hanging the query request.
 *
 * If this becomes a problem and our statistics query response times go up a lot,
 * we can trigger the statistics recalculation from the DeleteTransactionTaskHandler.
 */
public class DeleteTransactionTaskHandler implements TaskHandler<DeleteTransactionTask> {

    private static final LogUtils LOG = new LogUtils(DeleteTransactionTaskHandler.class);

    private final TransactionDao transactionDao;
    private final TransactionExternalIdRepository transactionExternalIdRepository;

    private final DeleteTransactionsHelper deleteTransactionsHelper;
    private static final Joiner JOINER = Joiner.on(", ");

    public DeleteTransactionTaskHandler(ServiceContext serviceContext) {
        this.transactionDao = serviceContext.getDao(TransactionDao.class);
        this.transactionExternalIdRepository = serviceContext
                .getRepository(TransactionExternalIdRepository.class);

        deleteTransactionsHelper = new DeleteTransactionsHelper(
                serviceContext,
                serviceContext.getCoordinationClient(),
                serviceContext.getRepository(UserStateRepository.class));
    }

    @Override
    public Set<Class<? extends DeleteTransactionTask>> handles() {
        return ImmutableSet.of(DeleteTransactionTask.class);
    }

    @Override
    public void handle(Task<?> deserializedPayload) {
        Preconditions.checkArgument(deserializedPayload instanceof DeleteTransactionTask);

        DeleteTransactionTask deleteTransactionTask = (DeleteTransactionTask) deserializedPayload;
        DeleteTransactionRequest deleteTransactionRequest = deleteTransactionTask.getPayload();

        Preconditions.checkArgument(!Strings.isNullOrEmpty(deleteTransactionRequest.getUserId()));
        Preconditions.checkArgument(deleteTransactionRequest.getTransactions() != null);

        final String userId = deleteTransactionRequest.getUserId();
        List<TransactionExternalId> transactionExternalIds = Lists.newArrayList();

        final Set<String> externalTransactionIds = deleteTransactionRequest.getTransactions().stream()
                .map(t -> {
                    Preconditions.checkArgument(!Strings.isNullOrEmpty(t.getExternalId()));
                    Preconditions.checkArgument(!Strings.isNullOrEmpty(t.getAccountId()));

                    // This is a way of logging that the delete has happened, and must therefore be executed before
                    // we look up the Tink transactions.
                    TransactionExternalId transactionExternalId = transactionExternalIdRepository
                            .findByAccountIdUserIdAndExternalTransactionId(t.getAccountId(), userId, t.getExternalId());
                    if (transactionExternalId == null) {
                        transactionExternalId = new TransactionExternalId();
                        transactionExternalId.setUserId(deleteTransactionRequest.getUserId());
                        transactionExternalId.setExternalTransactionId(t.getExternalId());
                        transactionExternalId.setAccountId(t.getAccountId());
                    }
                    transactionExternalId.setDeleted(true);
                    transactionExternalId.setUpdatedToNow();
                    transactionExternalIds.add(transactionExternalId);

                    return t.getExternalId();
                }).collect(Collectors.toSet());

        transactionExternalIdRepository.save(transactionExternalIds);

        // TODO With the new transaction_external_id table in Cassandra we should be able to invert this process
        List<String> transactionIds = transactionDao.findAllByUserId(userId)
                .stream()
                .filter(t -> externalTransactionIds.contains(t.getPayloadValue(TransactionPayloadTypes.EXTERNAL_ID)))
                .map(Transaction::getId)
                .collect(Collectors.toList());

        if (transactionIds.size() == 0) {
            LOG.warn(userId, "Couldn't find any transactions with externalIds: " + JOINER.join(externalTransactionIds));
            return;
        }

        if (transactionIds.size() != externalTransactionIds.size()) {
            LOG.warn(userId, String.format("Found %s transactions that matched the %s in: %s",
                    transactionIds.size(), externalTransactionIds.size(), JOINER.join(externalTransactionIds)));
            // Continue and delete them all anyway
        }

        deleteTransactionsHelper.delete(userId, transactionIds);
    }
}
