package se.tink.backend.system.cli.benchmark;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.ServiceContextCommand;

public class BenchmarkLoadingTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    public BenchmarkLoadingTransactionsCommand() {
        super("benchmark-transactions-read", "Benchmark reading transactions from Cassandra.");
    }
    
    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {

        // Repositories

        final TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        // Results.

        CLIUserBenchmarkRunner.run(userRepository, user -> transactionDao.findAllByUser(user));
    }

}
