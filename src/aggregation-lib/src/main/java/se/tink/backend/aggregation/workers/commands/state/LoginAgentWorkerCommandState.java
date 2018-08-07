package se.tink.backend.aggregation.workers.commands.state;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import se.tink.backend.aggregation.workers.metrics.TimerCacheLoader;
import se.tink.backend.common.ServiceContext;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class LoginAgentWorkerCommandState {


    public static final String LOAD_PERSISTENT_SESSION_TIMER_NAME = "load_persistent_session_duration";
    public static final String LOCK_TIMER_NAME = "acquire_lock_duration";
    public static final String RELEASE_LOCK_TIMER_NAME = "release_lock_duration";
    public static final String LOGIN_TIMER_NAME = "login_duration";
    public static final String LOGOOUT_TIMER_NAME = "logout_duration";

    private LoadingCache<MetricId.MetricLabels, Timer> loadPersistentSessionTimer;
    private LoadingCache<MetricId.MetricLabels, Timer> lockTimer;
    private LoadingCache<MetricId.MetricLabels, Timer> releaseLockTimer;
    private LoadingCache<MetricId.MetricLabels, Timer> loginTimer;
    private LoadingCache<MetricId.MetricLabels, Timer> logoutTimer;

    public LoginAgentWorkerCommandState(ServiceContext serviceContext,
                                        MetricRegistry metricRegistry) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        loadPersistentSessionTimer = cacheBuilder
                .build(new TimerCacheLoader(metricRegistry, LOAD_PERSISTENT_SESSION_TIMER_NAME));
        lockTimer = cacheBuilder.build(new TimerCacheLoader(metricRegistry, LOCK_TIMER_NAME));
        releaseLockTimer = cacheBuilder.build(new TimerCacheLoader(metricRegistry, RELEASE_LOCK_TIMER_NAME));
        loginTimer = cacheBuilder.build(new TimerCacheLoader(metricRegistry, LOGIN_TIMER_NAME));
        logoutTimer = cacheBuilder.build(new TimerCacheLoader(metricRegistry, LOGOOUT_TIMER_NAME));
    }

    public LoadingCache<MetricId.MetricLabels, Timer> getLockTimers() {
        return lockTimer;
    }

    public LoadingCache<MetricId.MetricLabels, Timer> getReleaseLockTimers() {
        return releaseLockTimer;
    }

    public LoadingCache<MetricId.MetricLabels, Timer> getLoginTimers() {
        return loginTimer;
    }

    public LoadingCache<MetricId.MetricLabels, Timer> getLogoutTimers() {
        return logoutTimer;
    }

    public LoadingCache<MetricId.MetricLabels, Timer> getLoadPersistentSessionTimers() {
        return loadPersistentSessionTimer;
    }
}