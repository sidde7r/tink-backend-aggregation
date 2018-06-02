package se.tink.backend.system.cli.benchmark;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.querybuilder.Select;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.time.YearMonth;
import java.util.List;
import java.util.concurrent.Future;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.data.cassandra.core.CassandraOperations;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class BenchmarkParallelReads extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(BenchmarkParallelReads.class);

    private final String tableName = "transactions_by_userid_period";
    private final List<Integer> allPeriods = DateUtils
            .getYearMonthPeriods(YearMonth.of(2000, 1), YearMonth.now().plusYears(1));

    public BenchmarkParallelReads() {
        super("benchmark-parallel-reads", "Benchmark reading transactions in parallel from Cassandra.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {

        // Repositories
        final CassandraOperations cassandraOperations = serviceContext.getCassandraOperations();
        final TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        // Results
        CLIUserBenchmarkRunner.run(userRepository, user -> {
            readAsync(cassandraOperations, user);
        });
    }

    private void readAsync(final CassandraOperations cassandraOperations, final User user) {
        final List<ResultSetFuture> futures = Lists.newArrayListWithExpectedSize(allPeriods.size());
        for (int period : allPeriods) {
            Select select = QueryBuilder.select().from(tableName);
            select.setConsistencyLevel(ConsistencyLevel.LOCAL_QUORUM);
            select.where(QueryBuilder.eq("userid", UUIDUtils.fromTinkUUID(user.getId())))
                    .and(QueryBuilder.eq("period", period));
            futures.add(cassandraOperations.executeAsynchronously(select));
        }
        Future<List<ResultSet>> future = Futures.successfulAsList(futures);
        try {
            // currently not mapping List<ResultSet> to List<CassandraTransactionByUserIdPeriod>
            // Block until future is done
            future.get();
        } catch (Exception e) {
            log.error("Exception reading from Cassandra", e);
        }
    }
}
