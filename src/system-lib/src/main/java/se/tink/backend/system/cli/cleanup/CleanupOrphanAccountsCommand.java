package se.tink.backend.system.cli.cleanup;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

/**
 * Deletes orphan accounts and their transactions.
 */
public class CleanupOrphanAccountsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(CleanupOrphanAccountsCommand.class);

    public CleanupOrphanAccountsCommand() {
        super("cleanup-orphan-accounts", "Delete orphan accounts and their transactions");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws IOException {

        final boolean dryRun = Boolean.getBoolean("dryRun");

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        AccountDao accountDao = serviceContext.getDao(AccountDao.class);
        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);

        userRepository
                .streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(user -> {
                    List<Account> orphanAccounts = accountRepository.findByUserId(user.getId())
                            .stream()
                            .filter(a -> credentialsRepository.findOne(a.getCredentialsId()) == null)
                            .collect(Collectors.toList());

                    if (!orphanAccounts.isEmpty()) {
                        log.info(String.format("Delete %d accounts for user %s and transactions (dryRun=%b).",
                                orphanAccounts.size(), user.getId(), dryRun));

                        if (!dryRun) {
                            for (Account account : orphanAccounts) {
                                log.info(String.format("Deleting account %s with its transactions", account.getId()));
                                accountDao.delete(account);
                            }
                        }
                    }
                });
    }
}
