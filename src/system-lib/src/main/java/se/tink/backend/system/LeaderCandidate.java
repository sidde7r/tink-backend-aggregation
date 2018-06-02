package se.tink.backend.system;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import se.tink.backend.common.config.SchedulerConfiguration;
import se.tink.backend.utils.LogUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

public class LeaderCandidate {
    private static final LogUtils log = new LogUtils(LeaderCandidate.class);
    private final SchedulerConfiguration schedulerConfiguration;
    private final CuratorFramework coordinationClient;
    private final AtomicBoolean isLeader = new AtomicBoolean(false);

    private LeaderSelector leaderSelector;

    @Inject
    public LeaderCandidate(SchedulerConfiguration schedulerConfiguration, CuratorFramework curatorFramework) {
        this.schedulerConfiguration = schedulerConfiguration;
        this.coordinationClient = curatorFramework;
    }

    public AtomicBoolean isCurrentInstanceLeader() {
        return isLeader;
    }

    @PostConstruct
    public void start() {
        leaderSelector = setupLeaderElection();
        leaderSelector.start();
    }

    @PreDestroy
    public void stop() {
        leaderSelector.close();
        leaderSelector = null;
    }

    private LeaderSelector setupLeaderElection() {
        final Semaphore connectionSemaphore = new Semaphore(1, true);

        LeaderSelectorListener listener = new LeaderSelectorListener() {
            @Override
            public void stateChanged(CuratorFramework client, ConnectionState state) {
                log.info("Got state change: " + state);

                switch (state) {
                case SUSPENDED:
                case LOST:
                case READ_ONLY:
                    connectionSemaphore.release();
                    break;
                default:
                    break;
                }
            }

            @Override
            public void takeLeadership(CuratorFramework client) throws Exception {
                log.info("Taking system leadership");

                connectionSemaphore.acquire();

                if (schedulerConfiguration.isEnabled()) {
                    isLeader.set(true);

                    connectionSemaphore.acquire();
                    connectionSemaphore.release();

                    log.info("Stepping down as leader due to connection loss");

                    isLeader.set(false);
                }
            }
        };

        LeaderSelector selector = new LeaderSelector(coordinationClient, "/leaders/system", listener);

        selector.autoRequeue();

        return selector;
    }
}
