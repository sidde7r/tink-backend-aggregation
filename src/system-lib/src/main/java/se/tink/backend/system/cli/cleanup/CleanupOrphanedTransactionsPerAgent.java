package se.tink.backend.system.cli.cleanup;

import com.google.common.base.Objects;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Provider;
import se.tink.backend.core.StatisticMode;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.User;
import se.tink.backend.system.api.ProcessService;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.system.rpc.GenerateStatisticsAndActivitiesRequest;
import se.tink.backend.utils.LogUtils;

public class CleanupOrphanedTransactionsPerAgent extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(CleanupOrphanedTransactionsPerAgent.class);
    private static final String DO_EXECUTE = "doExecute";
    private static final String AGENT_CLASS_NAME = "agentClass";

    private CredentialsRepository credentialsRepository;
    private AccountRepository accountRepository;
    private AccountDao accountDao;
    private TransactionDao transactionDao;
    private Map<String, Provider> providerNameMap;
    private ProcessService processService;

    public CleanupOrphanedTransactionsPerAgent() {
        super("cleanup-orphaned-transactions", "Delete orphaned transactions per agent.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final UserRepository userRepository = injector.getInstance(UserRepository.class);
        final ProviderRepository providerRepository = injector.getInstance(ProviderRepository.class);
        credentialsRepository = injector.getInstance(CredentialsRepository.class);
        accountRepository = injector.getInstance(AccountRepository.class);
        accountDao = serviceContext.getDao(AccountDao.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);
        processService = serviceContext.getSystemServiceFactory().getProcessService();

        final List<Provider> providerList = providerRepository.findAll();
        providerNameMap = providerList.stream().collect(Collectors.toMap(Provider::getName, Function.identity()));

        final AtomicInteger accountsCounter = new AtomicInteger();
        final String providerClassName = System.getProperty(AGENT_CLASS_NAME);
        final boolean doExecute = Boolean.getBoolean(DO_EXECUTE);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(u -> cleanupTransactions(doExecute, u, accountsCounter, providerClassName));

        log.info(String.format("Finished command. doExecute: [%s] - Deleted accounts: [%s]", String.valueOf(doExecute),
                accountsCounter.get()));
    }

    private String getProviderClassName(String providerName) {
        Provider provider = providerNameMap.getOrDefault(providerName, null);
        if (provider == null) {
            return null;
        }
        return provider.getClassName();
    }

    private void cleanupTransactions(boolean doExecute, User user, AtomicInteger accountsCounter,
            String providerClassName) {
        AtomicBoolean hasDeletedForUser = new AtomicBoolean(false);

        credentialsRepository.findAllByUserId(user.getId())
                .stream()
                .filter(c -> providerClassName.equals(getProviderClassName(c.getProviderName())))
                .forEach(credential -> {
                    Set<String> accountIdsPerCredential = accountRepository.findByCredentialsId(credential.getId())
                            .stream()
                            .map(Account::getId)
                            .collect(Collectors.toSet());

                    // Get all accountIds belonging to a credential based on the user's transactions.
                    Set<String> accountIdsPerTransactions = transactionDao.findAllByUser(user)
                            .stream()
                            .filter(t -> Objects.equal(credential.getId(), t.getCredentialsId()))
                            .map(Transaction::getAccountId)
                            .distinct()
                            .collect(Collectors.toSet());

                    // Delete all transactions for accounts that are in `accountIdsPerTransactions` but NOT in
                    // `accountIdsPerCredential`.
                    List<String> accountIdsToDelete = accountIdsPerTransactions.stream()
                            .filter(s -> !accountIdsPerCredential.contains(s))
                            .collect(Collectors.toList());

                    if (!accountIdsToDelete.isEmpty()) {
                        accountsCounter.addAndGet(accountIdsToDelete.size());
                        if (doExecute) {
                            accountIdsToDelete.forEach(
                                    accountId -> transactionDao.deleteByUserIdAndAccountId(user.getId(), accountId));
                            accountDao.deleteByUserIdAndAccountIds(user.getId(), accountIdsToDelete);
                            hasDeletedForUser.set(true);
                        }
                    }
                });

        // Update statistics if anything changed for the user.
        if (hasDeletedForUser.get()) {
            GenerateStatisticsAndActivitiesRequest request = new GenerateStatisticsAndActivitiesRequest();
            request.setMode(StatisticMode.FULL);
            request.setUserId(user.getId());
            processService.generateStatisticsAndActivityAsynchronously(request);
        }
    }
}
