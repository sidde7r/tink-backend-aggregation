package se.tink.backend.system.cli.gdpr;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.hash.Hashing;
import com.google.inject.Injector;
import com.lambdaworks.crypto.SCryptUtil;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.assertj.core.util.Strings;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.DeletedUserRepository;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

/**
 * Command that goes through all deleted users and check if we have deleted the user with the username input. This
 * command should not really be executed but is here to show that it is possible. We use scrypt as the hashing algorithm
 * but we have some (~25 users) that have been hashed with SHA512 instead of scrypt. (ErikP messed up).
 */
public class DeletedUsersVerificationCommand extends ServiceContextCommand<ServiceConfiguration> {

    public DeletedUsersVerificationCommand() {
        super("deleted-user-verification-command",
                "Command that verifies if we had a user with a certain username/email.");
    }

    private static final LogUtils log = new LogUtils(DeletedUsersVerificationCommand.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        String usernameInput = System.getProperty("username");
        Preconditions.checkNotNull(usernameInput);

        // Make sure the input is all in lower case without trailing whitespaces.
        final String username = usernameInput.toLowerCase().trim();

        final DeletedUserRepository deletedUserRepository = serviceContext.getRepository(DeletedUserRepository.class);

        for (DeletedUser deletedUser : deletedUserRepository.findAll()) {
            final String hash = deletedUser.getUsernameHash();

            if (Strings.isNullOrEmpty(hash)) {
                log.warn(deletedUser.getUserId(), "Deleted user does not have a hash.");
                continue;
            }

            if (isScryptHash(hash)) {
                if (SCryptUtil.check(username, deletedUser.getUsernameHash())) {
                    log.info(deletedUser.getUserId(), "User found.");
                }
            } else {
                // Special case for 25 users in Oxford production
                if (Hashing.sha512().hashString(username, Charsets.UTF_8).toString().equals(hash)) {
                    log.info(deletedUser.getUserId(), "User found.");
                }
            }
        }
    }

    private static boolean isScryptHash(String hash) {
        return hash.startsWith("$s");
    }
}
