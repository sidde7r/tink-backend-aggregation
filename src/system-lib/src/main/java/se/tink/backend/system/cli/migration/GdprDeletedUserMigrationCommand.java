package se.tink.backend.system.cli.migration;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import org.assertj.core.util.Strings;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.DeletedUserRepository;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class GdprDeletedUserMigrationCommand extends ServiceContextCommand<ServiceConfiguration> {

    public GdprDeletedUserMigrationCommand() {
        super("gdpr-deleted-user-migration-command", "Command that migrates data for deleted users.");
    }

    private static final LogUtils log = new LogUtils(GdprDeletedUserMigrationCommand.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final DeletedUserRepository deletedUserRepository = serviceContext.getRepository(DeletedUserRepository.class);

        for (DeletedUser deletedUser : deletedUserRepository.findAll()) {
            if (Strings.isNullOrEmpty(deletedUser.getUsername())) {
                continue;
            }

            // Calculate a hash of the username and reset the username
            deletedUser.setUsernameHash(DeletedUser.calculateUsernameHash(deletedUser.getUsername()));
            deletedUser.setUsername(null);

            deletedUserRepository.save(deletedUser);

            log.info(deletedUser.getUserId(), "Migrated user.");
        }
    }
}
