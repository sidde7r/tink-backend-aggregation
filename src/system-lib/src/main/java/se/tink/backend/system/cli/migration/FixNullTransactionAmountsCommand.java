package se.tink.backend.system.cli.migration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.CassandraTransactionDeleted;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class FixNullTransactionAmountsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(FixNullTransactionAmountsCommand.class);

    public FixNullTransactionAmountsCommand() {
        super("fix-transaction-null-amounts",
                "Restore transaction amount for the ones that are null.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final boolean dryRun = Boolean.getBoolean("dryRun");
        final TransactionDao indexedTransactionDAO = serviceContext.getDao(TransactionDao.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        final CassandraTransactionDeletedRepository cassandraTransactionDeletedRepository = serviceContext
                .getRepository(CassandraTransactionDeletedRepository.class);

        final LongAdder allTransactionsInspectedCount = new LongAdder();
        final LongAdder transactionsWithNullFoundCount = new LongAdder();
        final LongAdder repairedTransactionsCount = new LongAdder();
        final LongAdder deletedTransactionsCount = new LongAdder();
        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(user -> {
                    try {

                        List<Transaction> allUsersTransactions = indexedTransactionDAO.findAllByUser(user);
                        allTransactionsInspectedCount.add(allUsersTransactions.size());
                        Map<String, Transaction> transactions = allUsersTransactions
                                .stream()
                                .filter(t -> t.getAmount() == null)
                                .collect(Collectors.toMap(Transaction::getId, t -> t));

                        transactionsWithNullFoundCount.add(transactions.size());

                        Iterable<CassandraTransactionDeleted> deletedTransactions = cassandraTransactionDeletedRepository
                                .findByUserIdAndIds(
                                        user.getId(),
                                        transactions.keySet().stream().collect(Collectors.toList())
                                );

                        ImmutableList<CassandraTransactionDeleted> streamableDeletedTransactions = ImmutableList
                                .copyOf(deletedTransactions);
                        List<Transaction> toDelete = streamableDeletedTransactions
                                .stream()
                                .filter(t -> t.getAmount() == null && t.getExactAmount() == null)
                                .map(deleted -> transactions.get(UUIDUtils.toTinkUUID(deleted.getId())))
                                .collect(Collectors.toList());
                        List<Transaction> toSave = streamableDeletedTransactions
                                .stream()
                                .filter(t -> t.getAmount() != null || t.getExactAmount() != null)
                                .map(deleted -> {
                                    Double amount;
                                    if (deleted.getAmount() != null) {
                                        amount = deleted.getAmount();
                                    } else if (deleted.getOriginalAmount() != null) {
                                        amount = deleted.getOriginalAmount();
                                    } else if (deleted.getExactOriginalAmount() != null) {
                                        amount = deleted.getExactOriginalAmount().doubleValue();
                                    } else {
                                        amount = deleted.getExactAmount().doubleValue();
                                    }
                                    Transaction transaction = transactions.get(UUIDUtils.toTinkUUID(deleted.getId()));
                                    transaction.setAmount(amount);

                                    return transaction;
                                })
                                .collect(Collectors.toList());

                        repairedTransactionsCount.add(toSave.size());
                        deletedTransactionsCount.add(toDelete.size());

                        if (dryRun) {
                            for (Transaction t : toSave) {
                                log.debug(String.format("Would have saved: transactionId:%s userId:%s amount: %s",
                                        t.getId(), t.getUserId(), t.getAmount()));
                            }
                            for (Transaction t : toDelete) {
                                log.debug(String.format("Would have deleted: transactionId:%s userId:%s amount: %s",
                                        t.getId(), t.getUserId(), t.getAmount()));
                            }
                        } else {
                            for (Transaction t : toSave) {
                                log.debug(String.format("Saving: transactionId:%s userId:%s amount: %s",
                                        t.getId(), t.getUserId(), t.getAmount()));
                            }
                            if (!toSave.isEmpty()) {
                                indexedTransactionDAO.save(user, toSave);
                            }
                            for (Transaction t : toDelete) {
                                log.debug(String.format("Deleting: transactionId:%s userId:%s amount: %s",
                                        t.getId(), t.getUserId(), t.getAmount()));
                            }
                            if (!toDelete.isEmpty()) {
                                indexedTransactionDAO.delete(toDelete);
                            }
                        }

                    } catch (Exception e) {
                        log.error(user.getId(), "Command failed.", e);
                    }
                });

        log.info(String.format("Number of transactions inspected: %d", allTransactionsInspectedCount.longValue()));
        log.info(String.format("Number of transactions with null found: %d",
                transactionsWithNullFoundCount.longValue()));
        if (dryRun) {
            log.info(String.format("Number of transactions that would have been fixed: %d",
                    repairedTransactionsCount.longValue()));
            log.info(String.format("Number of transactions that would have been deleted: %d",
                    deletedTransactionsCount.longValue()));
        } else {
            log.info(String.format("Number of transactions fixed: %d", repairedTransactionsCount.longValue()));
            log.info(String.format("Number of transactions deleted: %d", deletedTransactionsCount.longValue()));
        }
    }

}

