package se.tink.backend.system.cli.migration;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class MigrateCSNAccountBankId extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(MigrateCSNAccountBankId.class);
    
    private Pattern newBankIdFormat = Pattern.compile("^\\d{12}: .*"); 
    
    public MigrateCSNAccountBankId() {
        super("migrate-csn-account-bankid", "Migrates `Account.bankId` for CSN credentials to the new format.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final boolean dryRun = Boolean.getBoolean("dryRun");

        log.info(String.format("Migrate CSN accounts (dryRun=%s).", dryRun));
        
        final CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        final AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);
        
        List<Credentials> csnCredentials = Lists.newArrayList(Iterables.filter(credentialsRepository.findAll(),
                c -> "csn".equals(c.getProviderName())));
        
        int migrated = 0;
        int skipped = 0;
        int replaced = 0;
        
        for (Credentials credentials : csnCredentials) {
            List<Account> accounts = accountRepository.findByCredentialsId(credentials.getId());

            for (Account account : accounts) {

                Matcher matcher = newBankIdFormat.matcher(account.getBankId());

                if (matcher.matches()) {
                    skipped++;
                    log.info(credentials.getUserId(), credentials.getId(),
                            String.format("Skipped account=%s (already migrated).", account.getId()));
                } else {
                    String newBankId = String.format("%s: %s", credentials.getUsername(), account.getBankId());

                    Account existingAccount = accountRepository.findByUserIdAndCredentialsIdAndBankId(
                            credentials.getUserId(), credentials.getId(), newBankId);

                    // A newer account instance exists. Remove it, and migrate the old one.
                    if (existingAccount != null) {
                        replaced++;
                        
                        if (!dryRun) {
                            accountRepository.delete(existingAccount);
                        }
                    }
                    
                    account.setBankId(newBankId);
                    if (!dryRun) {
                        accountRepository.save(account);
                    }
                    
                    migrated++;
                    
                    if (existingAccount != null) {
                        log.info(credentials.getUserId(), credentials.getId(), String.format(
                                "Migrated account=%s (replaced account=%s).", account.getId(), existingAccount.getId()));
                    } else {
                        log.info(credentials.getUserId(), credentials.getId(),
                                String.format("Migrated account=%s.", account.getId()));
                    }
                }
            }
        }

        log.info(String.format("Done! (migrated=%d, replaced=%d, skipped=%d)", migrated, replaced, skipped));
    }
}
