package se.tink.backend.system.cli.benchmark;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.ServiceContextCommand;

/**
 * To test the performance of reading using Observables. the current reading
 * implementation is just a wrapper around blocking code but changes will be
 * done and the benchmark will be needed.
 */
public class BenchmarkStreamingTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    public BenchmarkStreamingTransactionsCommand() {
        super("benchmark-transactions-stream", "Benchmark streaming transactions from Cassandra.");
    }
    
    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {

        // Repositories

        final TransactionDao transactionRepository = serviceContext.getDao(TransactionDao.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        // Results

        CLIUserBenchmarkRunner.run(userRepository, user -> {
            String userId = user.getId();

            transactionRepository.findAllByUser(user);
        });
    }

}
