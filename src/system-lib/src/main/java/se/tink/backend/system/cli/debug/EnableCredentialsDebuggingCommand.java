package se.tink.backend.system.cli.debug;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Calendar;
import java.util.Date;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.CredentialsRequestRunnableFactory;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.User;
import se.tink.backend.rpc.RefreshCredentialsRequest;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.libraries.date.DateUtils;

public class EnableCredentialsDebuggingCommand extends ServiceContextCommand<ServiceConfiguration> {
    public EnableCredentialsDebuggingCommand() {
        super("enable-credentials-debugging",
                "Enable debugging of a specific credentials for a limitted amount of time.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input handling
        final String credentialsId = System.getProperty("credentialsId");
        System.out.println("credentialsId to search for is: " + credentialsId);

        Integer numberOfDaysToDebug = Integer.getInteger("numberOfDaysToDebug", 30);
        System.out.println("numberOfDaysToDebug to debug the credentials is: " + numberOfDaysToDebug);

        // Input validation

        Preconditions.checkNotNull(credentialsId, "credentialsId must not be null.");
        Preconditions.checkArgument(numberOfDaysToDebug > 0, "numberOfDaysToDebug must be positive.");
        Preconditions
                .checkArgument(numberOfDaysToDebug < (365 / 2),
                        "numberOfDaysToDebug shouldn't be more than 6 months or something is broken. This is a security assertion.");

        // Calculate the date until we should debug

        final Date debugUntilDate = daysLaterFromToday(numberOfDaysToDebug);

        // Make the actual change.

        CredentialsRepository credentialsRepository = serviceContext.getRepository(CredentialsRepository.class);
        
        Credentials credentials = credentialsRepository.findOne(credentialsId);
        Preconditions
                .checkNotNull(credentials, String.format("credentials with id '%s' was not found.", credentialsId));

        credentials.setDebugUntil(debugUntilDate);

        credentialsRepository.save(credentials);

        // Tell the boss we are done.
        
        System.out.println(String.format("Credentials with id '%s' now has debugging enabled for %d days.",
                credentialsId, numberOfDaysToDebug));

        if (Boolean.getBoolean("refresh")) {
            refreshCredential(serviceContext, credentials);
        } else {
            System.out.println("Will not refresh the credential: System property 'refresh=false'.");
        }
    }

    private void refreshCredential(ServiceContext serviceContext, Credentials credentials) {
        if (!Objects.equal(credentials.getType(), CredentialsTypes.PASSWORD)) {
            System.out.println("Will not refresh the credential: Non-password credential.");
            return;
        }

        if (Strings.isNullOrEmpty(credentials.getUserId())) {
            System.out.println("Will not refresh the credential: Credential has no userId.");
            return;
        }

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        User user = userRepository.findOne(credentials.getUserId());
        if (user == null) {
            System.out.println("Will not refresh the credential: Could not find user for credential.");
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
            System.out.println(String.format(
                    "Refreshing credential: %s (user: %s).",
                    credentials.getId(),
                    user.getId()));

            runnable.run();
        } else {
            System.out.println("Will not refresh the credential: No runnable created.");
        }
    }

    private static Date daysLaterFromToday(Integer numberOfDaysToDebug) {
        final Date debugUntilDate;
        Calendar cal = DateUtils.getCalendar();
        cal.add(Calendar.DATE, numberOfDaysToDebug);
        debugUntilDate = cal.getTime();
        return debugUntilDate;
    }


}
