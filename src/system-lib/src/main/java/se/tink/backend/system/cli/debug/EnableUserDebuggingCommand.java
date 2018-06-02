package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.UUID;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class EnableUserDebuggingCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(EnableUserDebuggingCommand.class);
    private UserRepository userRepository;

    public EnableUserDebuggingCommand() {
        super("enable-user-debugging", "Enable debugging of a specific user id for a limitted amount of time.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        userRepository = serviceContext.getRepository(UserRepository.class);

        String userId = getAndValidateUserId();
        log.info("userId to search for is: " + userId);

        Integer numberOfDaysToDebug = getAndValidateDaysToDebug();
        log.info("numberOfDaysToDebug to debug the user is: " + numberOfDaysToDebug);

        Date debugUntilDate = DateUtils.addDays(new Date(), numberOfDaysToDebug);

        User user = getUser(userId);
        user.setDebugUntil(debugUntilDate);
        userRepository.save(user);

        log.info(
                String.format("User with id '%s' now has debugging enabled for %d days.", userId, numberOfDaysToDebug));
    }

    /**
     * Note: Convert to UUID and back to String will give validation of UUID input.
     */
    private static String getAndValidateUserId() {
        final UUID userId = UUIDUtils.fromTinkUUID(System.getProperty("userId"));

        // Validation
        Preconditions.checkNotNull(userId, "userId must not be null.");

        return UUIDUtils.toTinkUUID(userId);
    }

    private Integer getAndValidateDaysToDebug() {
        Integer numberOfDaysToDebug = Integer.getInteger("numberOfDaysToDebug", 14);

        // Validation
        Preconditions.checkArgument(numberOfDaysToDebug > 0, "numberOfDaysToDebug must be positive.");
        Preconditions.checkArgument(numberOfDaysToDebug < (365 / 2),
                "numberOfDaysToDebug shouldn't be more than 6 months or something is broken. This is a security assertion.");

        return numberOfDaysToDebug;
    }

    private User getUser(String userId) {
        User user = userRepository.findOne(userId);
        Preconditions.checkNotNull(user, String.format("user with id '%s' was not found.", userId));
        return user;
    }
}
