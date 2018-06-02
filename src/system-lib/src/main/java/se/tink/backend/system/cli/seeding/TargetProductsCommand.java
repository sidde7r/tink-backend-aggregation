package se.tink.backend.system.cli.seeding;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.product.targeting.TargetProductsController;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.system.cli.helper.traversal.CommandLineInterfaceUserTraverser;
import se.tink.backend.utils.LogUtils;

public class TargetProductsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final String DRY_RUN_PROPERTY_NAME = "dryRun";
    private static final String VERBOSE_PROPERTY_NAME = "verbose";
    private static final LogUtils log = new LogUtils(TargetProductsCommand.class);

    public TargetProductsCommand() {
        super("target-products", "");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        final TargetProductsController controller = new TargetProductsController(serviceContext);
        controller.setDryRun(Boolean.getBoolean(DRY_RUN_PROPERTY_NAME));
        controller.setVerbose(Boolean.getBoolean(VERBOSE_PROPERTY_NAME));

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

        userRepository.streamAll()
                .compose(new CommandLineInterfaceUserTraverser(10))
                .forEach(controller::process);
    }
}
