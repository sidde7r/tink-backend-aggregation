package se.tink.backend.system.cli.abnamro;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.abnamro.migration.AbnAmroCredentialsMigrator;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.controllers.DeleteController;
import se.tink.backend.common.dao.AccountDao;
import se.tink.backend.common.dao.transactions.TransactionDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

/**
 * Command that is used for migrating from old credential structure to new one. See `AbnAmroCredentialsMigrator`
 * for details. NB! DO NOT RUN BEFORE OLD GRIP APPS ARE DEPRECATED.
 */
public class AbnAmroCredentialsMigrationCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(AbnAmroCredentialsMigrationCommand.class);

    public AbnAmroCredentialsMigrationCommand() {
        super("abnamro-migrate-credentials", "Migrate old ICS and ABN credentials to one new credential.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final AbnAmroCredentialsMigrator migrator = new AbnAmroCredentialsMigrator(
                serviceContext.isUseAggregationController(),
                serviceContext.getAggregationControllerCommonClient(),
                serviceContext.getRepository(CredentialsRepository.class),
                serviceContext.getRepository(ProviderRepository.class),
                serviceContext.getDao(AccountDao.class),
                serviceContext.getDao(TransactionDao.class),
                serviceContext.getAggregationServiceFactory(),
                new DeleteController(serviceContext),
                serviceContext.isProvidersOnAggregation());

        serviceContext.getRepository(UserRepository.class).streamAll()
                .compose(new CommandLineInterfaceUserTraverser(1))
                .forEach(user -> {
                    try {
                        migrator.migrate(user);
                    } catch (Exception e) {
                        log.error(user.getId(), "Migration failed.", e);
                    }
                });
    }
}
