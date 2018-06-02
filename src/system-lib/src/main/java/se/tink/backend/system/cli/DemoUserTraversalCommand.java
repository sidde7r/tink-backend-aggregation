package se.tink.backend.system.cli;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

public class DemoUserTraversalCommand extends ServiceContextCommand<ServiceConfiguration> {

    public DemoUserTraversalCommand() {
        super("demo-user-traversal",
                "Command that demos how one can iterate over all users using Tink best practises.");
    }

    private static final LogUtils log = new LogUtils(DemoUserTraversalCommand.class);

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(20))
                .forEach(user -> {
                    try {

                        // PLACEHOLDER: Implement user handling here!

                    } catch (Exception e) {
                        log.error(user.getId(), "Command failed.", e);
                    }
                });

    }

}
