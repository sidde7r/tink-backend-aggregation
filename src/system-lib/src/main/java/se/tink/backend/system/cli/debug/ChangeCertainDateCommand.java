package se.tink.backend.system.cli.debug;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import org.joda.time.DateTime;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.User;
import se.tink.backend.rpc.RefreshCredentialsRequest;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class ChangeCertainDateCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(ChangeCertainDateCommand.class);

    public ChangeCertainDateCommand() {
        super("change-certain-date",
                "Change certain date of credential's accounts to fetch more transactions from earlier dates if user "
                        + "has duplicates");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input handling
        final String credentialsId = System.getProperty("credentialsId");
        log.info("credentialsId to search for is: " + credentialsId);

        Integer numberOfDaysToRemove = Integer.getInteger("numberOfDaysToRemove", 30);
        log.info("numberOfDaysToRemove to remove from the the certain date is: " + numberOfDaysToRemove);

        Boolean setCertainDateToNull = Boolean.getBoolean("setCertainDateToNull");
        log.info("setCertainDateToNull is: " + setCertainDateToNull);

        // Input validation

        Preconditions.checkNotNull(credentialsId, "credentialsId must not be null.");
        if (!setCertainDateToNull) {
            Preconditions.checkArgument(numberOfDaysToRemove > 0, "numberOfDaysToRemove must be positive.");
        }

        // Make the actual change

        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);

        Credentials credentials = credentialsRepository.findOne(credentialsId);
        Preconditions
                .checkNotNull(credentials, String.format("credentials with id '%s' was not found.", credentialsId));

        AccountRepository accountRepository = serviceContext.getRepository(AccountRepository.class);

        List<Account> accounts = accountRepository.findByCredentialsId(credentials.getId());
        Preconditions.checkState(!accounts.isEmpty(),
                String.format("Accounts for credential with id '%s' was not found.", credentials.getId()));

        if (setCertainDateToNull) {
            updateCertainDateToNull(accounts);
        } else {
            updateCertainDate(accounts, numberOfDaysToRemove);
        }
        accountRepository.save(accounts);

        // Tell the boss we are done.

        log.info(credentials, "Certain dates updated for accounts.");

        if (Boolean.getBoolean("refresh") && Objects.equal(credentials.getType(), CredentialsTypes.PASSWORD)) {
            refreshCredential(serviceContext, credentials);
        } else {
            log.info("Will not refresh the credential.");
        }

    }

    private void updateCertainDateToNull(List<Account> accounts) {
        accounts.forEach(a -> {
                                log.info(a, "Setting certain date to null");
                                a.setCertainDate(null);
        });
    }

    private void updateCertainDate(List<Account> accounts, int numberOfDaysToRemove) {
        for (Account account : accounts) {
            if (account.getCertainDate() == null) {
                continue;
            }

            log.info(account, "Change certain date");

            DateTime oldCertainDate = new DateTime(account.getCertainDate());
            DateTime newCertainDate = getNewCertainDate(oldCertainDate, numberOfDaysToRemove);

            if (Objects.equal(oldCertainDate, newCertainDate)) {
                log.info(account, "Certain date will not be updated");
                continue;
            }

            account.setCertainDate(newCertainDate.toDate());
            log.info(account, String.format(
                    "Certain date updated: {old: %s new: %s}", oldCertainDate, newCertainDate));
        }
    }

    private void refreshCredential(ServiceContext serviceContext, Credentials credentials) {
        if (Strings.isNullOrEmpty(credentials.getUserId())) {
            log.info("Will not refresh the credential: Credential has no userId.");
            return;
        }

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        User user = userRepository.findOne(credentials.getUserId());
        if (user == null) {
            log.info("Will not refresh the credential: Could not find user for credential.");
            return;
        }

        RefreshCredentialsRequest refreshCredentialsRequest = new RefreshCredentialsRequest();
        refreshCredentialsRequest.setCredentials(Lists.newArrayList(credentials));

        CredentialsRequestRunnableFactory refreshCredentialsFactory = new CredentialsRequestRunnableFactory(serviceContext);

        // Need to push this status to something other than UPDATED, TEMP_ERROR and AUTH_ERROR since we want to forcefully update the credential
        // The mentioned statuses have maximum refresh rate of once per day
        credentials.setStatus(CredentialsStatus.AUTHENTICATING);
        Runnable runnable = refreshCredentialsFactory.createRefreshRunnable(user, credentials, false, false, false);

        if (runnable != null) {
            log.info(credentials, "Refreshing credential");
            runnable.run();
        } else {
            log.info("Will not refresh the credential: No runnable created.");
        }
    }

    private static DateTime getNewCertainDate(DateTime oldCertainDate, Integer numberOfDaysToRemoveFromCertainDate) {
        return oldCertainDate.minusDays(numberOfDaysToRemoveFromCertainDate);
    }
}
