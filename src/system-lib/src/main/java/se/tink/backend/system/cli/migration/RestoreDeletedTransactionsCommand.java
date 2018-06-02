package se.tink.backend.system.cli.migration;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.CassandraTransactionDeletedRepository;
import se.tink.backend.common.utils.CassandraTransactionConverter;
import se.tink.backend.core.CassandraTransactionDeleted;
import se.tink.backend.core.Transaction;
import se.tink.backend.system.cli.ServiceContextCommand;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RestoreDeletedTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final Logger log = LoggerFactory.getLogger(RestoreDeletedTransactionsCommand.class);
    private static final int batchSize = 25;
    private static final long delay = 60L;
    
    public RestoreDeletedTransactionsCommand() {
        super("restore-transactions",
                "Restore transaction for affected user");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
                       ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        String userIdsPath = System.getProperty("userIdsFile");
        Preconditions.checkArgument(userIdsPath != null,
                "Must specify file with user id(s) credential id(s).");
        File userIdsCredentialsFile = new File(userIdsPath);
        TransactionDao indexedTransactionDAO = serviceContext.getDao(TransactionDao.class);

        Map<String, List<String>> userIdTransactions = StreamSupport.stream(Spliterators.spliteratorUnknownSize(CSVParser
                .parse(userIdsCredentialsFile, StandardCharsets.UTF_8, CSVFormat.DEFAULT)
                .iterator(), 0), false)
                .collect(Collectors.groupingBy(row -> row.get(0), Collectors.mapping(row -> row.get(1), Collectors.toList())));
        CassandraTransactionDeletedRepository cassandraTransactionDeletedRepository = serviceContext.getRepository(CassandraTransactionDeletedRepository.class);

        userIdTransactions.forEach( (userId, transactionIds) -> {
            try {
                Iterable<CassandraTransactionDeleted> deletedCassandraTransactions = cassandraTransactionDeletedRepository.findByUserIdAndIds(userId, transactionIds);
                List<Transaction> transactionsToRestore = StreamSupport.stream(Spliterators.spliteratorUnknownSize(deletedCassandraTransactions.iterator(), 0), false)
                        .map(t -> CassandraTransactionConverter.fromCassandraTransactionDeleted(t))
                        .filter(t -> t.getCategoryId() != null).collect(Collectors.toList());

                Lists.partition(transactionsToRestore, batchSize)
                        .forEach(batch -> {
                            try {
                                Thread.sleep(delay);
                                indexedTransactionDAO.saveAndIndex(userId, batch, true);
                            } catch (InterruptedException e) {
                                log.error(e.getMessage(), e);
                            }
                        });
                log.info("number of transactions restored {} for user {}", transactionsToRestore.size(), userId);
            } catch (Exception e) {
                log.error(String.format("failed to restore transaction for userId %s transaction Id %s\n %s", userId, transactionIds, e.getMessage()), e);
            }
        });
    }

}

