package se.tink.backend.system.cli.migration;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

/**
 * a recent migration of amex created duplicated accounts in two different bankId formats:
 * 1) old gen format -> 'XXX-11111'
 * 2) new gen format where no non numeric and alphabet symbels are removed 'XXX11111'
 *
 * this command intend to find all the duplicated accounts, and since the one with the new gen format
 * should be linked to more transactions, the account with '-' should be removed
 *
 * additionally, we want to update all account BankId to the new format even there were not duplicates
 * created.
 **/
public class MigrateAmexBankId extends ServiceContextCommand<ServiceConfiguration> {

    AccountRepository accountRepository;

    public MigrateAmexBankId() {
        super("migrate-amex-bankid", "Migrates `accounts.bankid` field to new gen format.");
    }

    private boolean actuallyExecute = Boolean.getBoolean("execute");
    private static final LogUtils log = new LogUtils(MigrateAmexBankId.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        final CredentialsRepository credentialsRepository = injector.getInstance(CredentialsRepository.class);
        accountRepository = injector.getInstance(AccountRepository.class);
        List<Credentials> amexCredentials;

        amexCredentials = credentialsRepository.findAll().stream()
                .filter(c -> "americanexpress".equals(c.getProviderName()))
                .collect(Collectors.toList());
        migrationOnCredentials(amexCredentials);

        amexCredentials = credentialsRepository.findAll().stream()
                .filter(c -> "saseurobonusamericanexpress".equals(c.getProviderName()))
                .collect(Collectors.toList());
        migrationOnCredentials(amexCredentials);

    }

    private void migrationOnCredentials(List<Credentials> amexCredentials) {

        // put all duplicate accounts in a list mapped by new gen bankId format
        for (Credentials credentials : amexCredentials) {
            Map<String, List<Account>> accountsByBankId = new HashMap<>();
            List<Account> accounts = accountRepository.findByCredentialsId(credentials.getId());
            log.info("\nMigrate credential " + credentials.getId() + " contains accounts: " +
                    printMultipleAccounts(accounts));

            accounts.forEach(account -> {
                String newGenBankId = account.getBankId().replace("-", "");
                List<Account> accountList = accountsByBankId.get(newGenBankId);
                if (accountList == null) {
                    accountList = new LinkedList<>();
                    accountList.add(account);
                    accountsByBankId.put(newGenBankId, accountList);
                } else {
                    accountList.add(account);
                }
            });
            accountsByBankId.forEach(this::removeDuplicateAccount);
        }
    }

    /**
     * removing duplicate accounts has logic:
     * check if there are duplicates
     * if there isn't, update the account to new gen format
     * else
     * find the least recently created account and delete
     **/
    private void removeDuplicateAccount(String newGenBankId, List<Account> accountsWithSameBankId) {
        if (accountsWithSameBankId.size() < 2) {
            Account account = accountsWithSameBankId.get(0);
            if (!account.getBankId().equals(newGenBankId)) {
                log.info("\n" + printAccount(account) + "\nnot duplicate\tneeds to update");
                account.setBankId(newGenBankId);
                if (actuallyExecute) {
                    accountRepository.save(account);
                }
                log.info("\naccount updates to: " + printAccount(account));
            } else {
                log.info("\n" + printAccount(account) + "\nnot duplicate\tno needs to update");
            }
        } else {
            log.info("\nduplicated accounts found:" + printMultipleAccounts(accountsWithSameBankId));
            accountsWithSameBankId.forEach(a -> {
                if (a.getBankId().contains("-")) {
                    if (actuallyExecute) {
                        accountRepository.delete(a);
                    }
                    log.info("\n" + printAccount(a) + "\nduplicate\tneed to remove");
                } else {
                    log.info("\n" + printAccount(a) + "\nduplicate\tno need to remove");
                }

            });
        }
    }

    private String printMultipleAccounts(List<Account> accounts) {
        StringBuilder toPrint = new StringBuilder();
        accounts.forEach(account -> toPrint.append("\n").append(printAccount(account)));
        return toPrint.toString();
    }

    private String printAccount(Account account) {
        return String.format("%s\t%s\t%s\t%s\t%s\t%s",
                account.getId(),
                account.getAccountNumber(),
                account.getBankId(),
                account.getCredentialsId(),
                account.getName(),
                account.getUserId()
        );
    }
}