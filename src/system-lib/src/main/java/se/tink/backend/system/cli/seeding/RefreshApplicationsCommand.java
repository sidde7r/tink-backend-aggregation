package se.tink.backend.system.cli.seeding;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;

public class RefreshApplicationsCommand extends ServiceContextCommand<ServiceConfiguration> {
    public RefreshApplicationsCommand() {
        super("refresh-applications", "");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        RefreshApplicationsController refreshApplicationsController = new RefreshApplicationsController(serviceContext);

        serviceContext.getRepository(UserRepository.class).streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(refreshApplicationsController::refreshApplications);
    }
}
