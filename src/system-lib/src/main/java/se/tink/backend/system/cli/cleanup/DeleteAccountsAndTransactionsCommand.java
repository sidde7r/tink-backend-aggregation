package se.tink.backend.system.cli.cleanup;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

/**
 * Deletes accounts and their transactions.
 * Account IDs to delete are specified in accountid-filter.txt. Arguments are passed via system properties.
 * If {@code dryRun} is equal to {@code true}, have no effect. Assumed by default.
 * If {@code deleteAccounts} is true deletes accounts. Only transactions are deleted otherwise.
 */
public class DeleteAccountsAndTransactionsCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(DeleteAccountsAndTransactionsCommand.class);

    public DeleteAccountsAndTransactionsCommand() {
        super("cleanup-transactions-from-accountid", "Delete transactions alongside related accounts");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws IOException {

        File accountIdFilterFile = new File("accountid-filter.txt");
        if (!accountIdFilterFile.exists()) {
            log.info("File " + accountIdFilterFile.getName() + " does not exist. Quitting");
            return;
        }

        final String dryRunProperty = System.getProperty("dryRun");
        final boolean dryRun = dryRunProperty == null || Boolean.parseBoolean(dryRunProperty);

        run(dryRun, Boolean.getBoolean("deleteAccounts"),
                Files.readLines(accountIdFilterFile, Charsets.UTF_8),
                serviceContext.getDao(AccountDao.class),
                serviceContext.getDao(TransactionDao.class));
    }

    @VisibleForTesting
    void run(boolean dryRun, boolean deleteAccounts, List<String> accountIds,
            AccountDao accountDao, TransactionDao transactionDao) {
        log.info(String.format("Delete %d accounts and transactions (dryRun=%b, deleteAccounts=%b).",
                accountIds.size(), dryRun, deleteAccounts));
        if (!dryRun) {
            log.info("Deleting transactions");
            transactionDao.deleteByAccountIds(accountIds);
            if (deleteAccounts) {
                log.info("Deleting accounts");
                accountDao.deleteByIds(accountIds);
            }
        }
    }

}
