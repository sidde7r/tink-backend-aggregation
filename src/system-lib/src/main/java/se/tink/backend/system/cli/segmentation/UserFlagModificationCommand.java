package se.tink.backend.system.cli.segmentation;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.backend.system.api.UpdateService;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.utils.LogUtils;

public class UserFlagModificationCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(UserFlagModificationCommand.class);

    /*
     * Use `ModifyUserFlagCommand` instead.
     */
    @Deprecated
    public UserFlagModificationCommand() {
        super("user-flag-modification", "Modifies a flag to a user for userId or username");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input validation

        final String username = System.getProperty("username");
        final String userId = System.getProperty("userId");
        final String flag = System.getProperty("flag");
        final String action = System.getProperty("action");
        log.info("Username to search for is " + username);
        log.info("UserId to search for is " + userId);
        log.info("Flag to alter is " + flag);
        log.info("Alter action is " + action);

        Preconditions.checkArgument(Strings.nullToEmpty(username).trim().length() > 0 || Strings.nullToEmpty(userId).trim().length() > 0);
        Preconditions.checkArgument(Strings.nullToEmpty(flag).trim().length() > 0);
        Preconditions.checkArgument(Strings.nullToEmpty(action).trim().length() > 0);

        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        final UpdateService userService = injector.getInstance(SystemServiceFactory.class).getUpdateService();

        final User user = Strings.isNullOrEmpty(userId) ? userRepository.findOneByUsername(username) : userRepository.findOne(userId);

        if (user == null) {
            log.info("No user found for " + username + " or " + userId);
            return;
        }

        List<String> userFlags = user.getFlags();
        if (Objects.equal(action,"add")) {
            Preconditions.checkArgument(!userFlags.contains(flag), "Expected user not to have flag");
            userFlags.add(flag);
            log.info(user.getId(),
                    String.format("Adding %s to user with username: %s", flag, user.getUsername()));
        } else if (Objects.equal(action,"remove")) {
            Preconditions.checkArgument(userFlags.contains(flag), "Expected user to have flag");
            userFlags.remove(flag);
            log.info(user.getId(),
                    String.format("Removing %s from user with username: %s", flag, user.getUsername()));
        }

        userService.updateUserFlags(user.getId(), userFlags);
    }
}
