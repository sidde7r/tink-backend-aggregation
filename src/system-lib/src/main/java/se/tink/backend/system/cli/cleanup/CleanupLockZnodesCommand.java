package se.tink.backend.system.cli.cleanup;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.function.Predicate;
import net.sourceforge.argparse4j.inf.Namespace;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.data.Stat;
import org.joda.time.DateTime;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;

public class CleanupLockZnodesCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final String LOCK_PATH = "/locks";
    private static final LogUtils log = new LogUtils(CleanupLockZnodesCommand.class);

    public CleanupLockZnodesCommand() {
        super("cleanup-lock-znodes", "Cleanup lock Znodes.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, final ServiceContext serviceContext)
            throws Exception {

        CuratorFramework client = injector.getInstance(CuratorFramework.class);
        client.blockUntilConnected();

        final boolean dryRun = Boolean.getBoolean("dryRun");
        final int minutesFreshness = Integer.getInteger("minutesFreshness", 24 * 60);
        final long latestTimeModified = DateTime.now().minusMinutes(minutesFreshness).getMillis();
        Predicate<Stat> statFilter = stat -> stat != null && stat.getMtime() < latestTimeModified;
        delete(client, dryRun, statFilter, LOCK_PATH, false);
    }

    /**
     * Traverse the Zookeeper graph depth-first starting at {@code path} and delete ZNodes that pass {@code statFilter}
     * Somewhat inspired by {@link ZKPaths#deleteChildren}
     *
     * @param client     the curator client to use
     * @param dryRun     only print nodes that would have been deleted
     * @param statFilter the predicate that must pass to attempt deletion
     * @param path       the path of the Zookeeper node where the recursive deletion starts
     * @param deleteSelf
     */
    public static void delete(final CuratorFramework client, final boolean dryRun, final Predicate<Stat> statFilter,
            final String path, final boolean deleteSelf)
            throws Exception {
        List<String> children = client.getChildren().forPath(path);
        for (String child : children) {
            String childPath = ZKPaths.makePath(path, child);
            try {
                delete(client, dryRun, statFilter, childPath, true);
            } catch (Exception e) {
                /* If we can't delete something we probably didn't intend to do so anyway.
                 Possible reasons could be:
                   Someone already deleted this node
                   Someone just created a child node
                   There are child nodes that wasn't deleted because of freshness
                */
                log.info(String.format("Could not delete: %s", childPath));
            }
        }
        Stat stat = client.checkExists().forPath(path);
        if (statFilter.test(stat)) {
            if (dryRun) {
                log.info(String.format("Would have attempted deletion of: %s", path));
            } else if (deleteSelf) {
                client.delete().guaranteed().forPath(path);
                log.info(String.format("Deleted: %s", path));
            }
        }
    }
}
