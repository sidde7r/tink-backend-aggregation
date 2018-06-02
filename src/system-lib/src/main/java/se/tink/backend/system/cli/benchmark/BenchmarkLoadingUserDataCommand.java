package se.tink.backend.system.cli.benchmark;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.core.UserData;
import se.tink.backend.system.cli.ServiceContextCommand;

public class BenchmarkLoadingUserDataCommand extends ServiceContextCommand<ServiceConfiguration> {

    public BenchmarkLoadingUserDataCommand() {
        super("benchmark-user-data", "Benchmark building UserData.");
    }
    
    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {

        // Repositories

        final TransactionDao transactionDao = serviceContext.getDao(TransactionDao.class);
        final AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);
        final AccountBalanceHistoryRepository accountHistoryRepository = serviceContext
                .getRepository(AccountBalanceHistoryRepository.class);
        final CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        final UserStateRepository userStateRepository = serviceContext.getRepository(UserStateRepository.class);
        final LoanDataRepository loanDataRepository = serviceContext.getRepository(LoanDataRepository.class);
        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        // Results.

        CLIUserBenchmarkRunner.run(userRepository, user -> {
            String userId = user.getId();

            UserData userData = new UserData();
            userData.setUser(userRepository.findOne(userId));
            userData.setTransactions(transactionDao.findAllByUser(user));
            userData.setAccounts(accountRepository.findByUserId(userId));
            userData.setAccountBalanceHistory(accountHistoryRepository.findByUserId(userId));
            userData.setCredentials(credentialsRepository.findAllByUserId(userId));
            userData.setUserState(userStateRepository.findOneByUserId(userId));
            userData.setLoanDataByAccount(loanDataRepository.findAllByAccounts(userData.getAccounts()));
        });
    }
}
