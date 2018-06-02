package se.tink.backend.system.cli.cleanup;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.core.Account;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.StringUtils;

public class DeleteCredentialsAccountCommand extends ServiceContextCommand<ServiceConfiguration> {

    private AccountRepository accountRepository;
    private AccountDao accountDao;
    private TransactionDao transactionDao;
    private final static LogUtils log = new LogUtils(DeleteCredentialsAccountCommand.class);

    public DeleteCredentialsAccountCommand() {
        super("delete-credentials-account", "Delete an account for a credentials.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        accountRepository = serviceContext.getRepository(AccountRepository.class);
        accountDao = serviceContext.getDao(AccountDao.class);
        transactionDao = serviceContext.getDao(TransactionDao.class);

        // Input argument.

        final String accountId = System.getProperty("accountId");

        // Validate input arguments.
        Preconditions.checkArgument(StringUtils.trimToNull(accountId) != null);

        log.info("Deleting account and transactions for accountId: " + accountId);

        final Account account = accountRepository.findOne(accountId);
        Preconditions.checkNotNull(account, "Could not find the account.");
        accountDao.delete(account);
    }

}
