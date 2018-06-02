package se.tink.backend.system.cli.cleanup;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.FluentIterable;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.argparse4j.inf.Namespace;
import org.springframework.util.Assert;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.AccountBalanceHistoryRepository;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.AccountBalance;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class CleanupAccountHistoryCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(CleanupAccountHistoryCommand.class);
    private AccountRepository accountRepository;
    private CredentialsRepository credentialsRepository;
    private AccountBalanceHistoryRepository accountBalanceHistoryRepository;

    public CleanupAccountHistoryCommand() {
        super("cleanup-account-history-command", "Used to clean up account history if the balance has change in some way");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext) throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        this.accountRepository = serviceContext.getRepository(AccountRepository.class);
        this.credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        this.accountBalanceHistoryRepository = serviceContext.getRepository(AccountBalanceHistoryRepository.class);

        final String startTimeInput = System.getProperty("startTime");
        final String endTimeInput = System.getProperty("endTime");

        Preconditions.checkNotNull(startTimeInput, "Missing input for startTime (Format: yyyy-MM-dd HH:mm)");
        Preconditions.checkNotNull(endTimeInput, "Missing input for endTime (Format: yyyy-MM-dd HH:mm)");

        final Date startTime;
        final Date endTime;

        try {
            startTime = ThreadSafeDateFormat.FORMATTER_MINUTES.parse(startTimeInput);
            endTime = ThreadSafeDateFormat.FORMATTER_MINUTES.parse(endTimeInput);
        } catch (ParseException pe) {
            log.error("Could not parse date of expected format yyyy-MM-dd HH:mm");
            throw pe;
        }

        Assert.isTrue(startTime.before(endTime), "StartTime has to be before endTime");

        // What user(s) should we clean up for?
        final String users = System.getProperty("users");

        if (Strings.isNullOrEmpty(users)) {
            log.error("You need to specify which users to clean up. Either `users=all` or `users=<comma separated list of user ids>`.");
            return;
        }

        // Which provider should we clean up for?
        final String providerNamesInput = System.getProperty("providerName");

        if (Strings.isNullOrEmpty(providerNamesInput)) {
            log.error("You need to specify which provider(s) to clean up for. Either `providerName=provider` or `providerName=<comma separated list of providers>`.");
            return;
        }

        // Which account types should we clean up?
        final String accountTypesInput = System.getProperty("accountType");

        if (Strings.isNullOrEmpty(accountTypesInput)) {
            log.error("You need to specify which accountType(s) to clean up for. Either `accountType=accountType` or `accountType=<comma separated list of accountTypes>`.");
            return;
        }

        log.info(String.format("Cleanup account history (startTime=%s, endTime=%s, users=%s, providers=%s, accountTypes=%s).", startTime, endTime, users, providerNamesInput, accountTypesInput));

        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        if ("all".equalsIgnoreCase(users)) {
            userRepository.streamAll().forEach(user -> {
                int handledUsers = counter.getAndIncrement();
                if (handledUsers % 10000 == 0) {
                    log.info(String.format("Processed %s users", handledUsers));
                }
                cleanup(user, providerNamesInput, accountTypesInput, startTime, endTime);
            });
        } else {
            Iterable<String> userIds = Splitter.on(',').split(users);
            for (String userId : userIds) {
                User user = userRepository.findOne(userId);
                if (user == null) {
                    log.warn(userId, "User could not be found. Skipping.");
                    continue;
                }

                cleanup(user, providerNamesInput, accountTypesInput, startTime, endTime);
            }
        }

        log.info("Cleanup account history command has finished successfully.");
    }

    public void cleanup(final User user, String providerNamesInput,
            String accountTypesInput, final Date startTime, final Date endTime) {

        List<Credentials> credentials = credentialsRepository.findAllByUserId(user.getId());
        List<String> providerNames = Arrays.asList(providerNamesInput.toLowerCase().split(","));

        FluentIterable<Credentials> provider = FluentIterable.from(
                credentials).filter(credentialsForProviders(providerNames));

        if (provider.isEmpty()) {
            return;
        }

        for (Credentials credential : credentials) {
            List<Account> accounts = accountRepository
                    .findByUserIdAndCredentialsId(user.getId(), credential.getId());

            final List<String> accountTypes = Arrays.asList(accountTypesInput.toUpperCase().split(","));

            FluentIterable<Account> accountsToCleanup = FluentIterable.from(accounts)
                    .filter(accountsToCleanup(accountTypes));

            findAndDelete(user, accountsToCleanup, startTime, endTime);
        }
    }

    private void findAndDelete(final User user,
            FluentIterable<Account> accountsToCleanup, final Date startTime, final Date endTime) {

        for (Account account : accountsToCleanup) {
            List<AccountBalance> accountBalances =
                    accountBalanceHistoryRepository.findByUserIdAndAccountId(user.getId(), account.getId());

            FluentIterable<AccountBalance> accountBalancesToDelete = FluentIterable.from(accountBalances)
                    .filter(accountBalance -> accountBalance.getInserted() > startTime.getTime() &&
                            accountBalance.getInserted() < endTime.getTime());

            for (AccountBalance accountBalance : accountBalancesToDelete) {
                accountBalanceHistoryRepository.deleteByUserIdAndAccountIdAndDate(
                        accountBalance.getUserId(), accountBalance.getAccountId(), accountBalance.getDate());
            }
        }
    }

    private static Predicate<Credentials> credentialsForProviders(final List<String> providerNames) {
        return credentials -> providerNames.contains(credentials.getProviderName());
    }

    private static Predicate<Account> accountsToCleanup(final List<String> accountTypes) {
        return account -> accountTypes.contains(account.getType().name());
    }
}
