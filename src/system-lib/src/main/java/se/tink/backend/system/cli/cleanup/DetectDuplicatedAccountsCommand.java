package se.tink.backend.system.cli.cleanup;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class DetectDuplicatedAccountsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(DetectDuplicatedAccountsCommand.class);
    private static final String LEADING_ZERO = "^0+";
    private static final String MESSAGE_FORMAT = "Repeated %d accounts for userId:%s with credentialId:%s\n";

    public DetectDuplicatedAccountsCommand() {
        super("detect-duplicated-accounts", "Out how many and which accounts is duplicated by same credentials");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        log.info("Starting to detect duplicated accounts command");

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        final AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);
        final AtomicInteger changedCount = new AtomicInteger();

        userRepository.streamAll().forEach(user -> {
            Multimap<String, Account> duplicatedAccounts = extractDuplicatedAccounts(
                    accountRepository.findByUserId(user.getId()));
            changedCount.addAndGet(duplicatedAccounts.keySet().size());

            for (String key : duplicatedAccounts.keySet()) {
                repeatedAccountsToString(duplicatedAccounts.get(key));
            }

        });

        log.info(String.format("Detected %d duplicated accounts", changedCount.get()));
    }

    @VisibleForTesting
    Multimap<String, Account> extractDuplicatedAccounts(List<Account> accounts) {
        final Multimap<String, Account> accountsMap = accountsToMap(accounts);

        return Multimaps.filterKeys(accountsMap, key -> accountsMap.get(key).size() > 1);
    }

    private String repeatedAccountsToString(Collection<Account> accounts) {
        Account randomElement = Iterables.getLast(accounts);
        StringBuilder stringBuilder = new StringBuilder(
                String.format(MESSAGE_FORMAT, accounts.size(), randomElement.getUserId(),
                        randomElement.getCredentialsId()));

        int i = 1;

        for (Account account : accounts) {
            stringBuilder.append(i++)
                    .append(". AccountId: ")
                    .append(account.getId())
                    .append(", bankId: ")
                    .append(account.getBankId())
                    .append(", name: ")
                    .append(account.getName())
                    .append(", excluded: ")
                    .append(account.isExcluded())
                    .append("\n");
        }

        return stringBuilder.append("\n\n").toString();
    }

    private Multimap<String, Account> accountsToMap(List<Account> accounts) {
        return Multimaps.index(accounts, new Function<Account, String>() {
            @Nullable
            @Override
            public String apply(Account account) {
                return accountKey(account);
            }
        });
    }

    @VisibleForTesting
    String accountKey(Account account) {
        return account.getCredentialsId() + cleanUpId(account.getBankId());
    }

    @VisibleForTesting
    String cleanUpId(String id) {
        return CharMatcher.DIGIT.retainFrom(id).replaceAll(LEADING_ZERO, "");
    }
}
