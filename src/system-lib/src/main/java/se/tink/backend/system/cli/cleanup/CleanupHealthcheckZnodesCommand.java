package se.tink.backend.system.cli.cleanup;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class CleanupHealthcheckZnodesCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final String HEALTHCHECK_LOCK_PREFIX = "/locks/healthcheck";
    private static final String SLASHED_HEALTHCHECK_LOCK_PREFIX = HEALTHCHECK_LOCK_PREFIX + "/";
    private static final LogUtils log = new LogUtils(CleanupHealthcheckZnodesCommand.class);
    
    public CleanupHealthcheckZnodesCommand() {
        super("cleanup-healthcheck-znodes", "Cleanup znodes created by old healthcheck.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws Exception {

        CuratorFramework client = serviceContext.getCoordinationClient();
        client.blockUntilConnected();
        
        List<String> children = client.getChildren().forPath(HEALTHCHECK_LOCK_PREFIX);
        log.info(String.format(
                "Found %d children. Deleting them one by one (as long as they aren't being used by healthchecks).",
                children.size()));
        
        for (String name : children) {

            long nameNumber = 0;
            try {
                nameNumber = Long.parseLong(name);
            } catch (NumberFormatException e) {
                log.warn(String.format("Could not parse childname: %s", name));
                continue;
            }
            if (nameNumber > 255) {
                String toDelete = SLASHED_HEALTHCHECK_LOCK_PREFIX + name;

                log.info(String.format("Deleting: %s", toDelete));

                client.delete().guaranteed().deletingChildrenIfNeeded().forPath(toDelete);
            }
        }

        log.info("Done.");

    }
}
