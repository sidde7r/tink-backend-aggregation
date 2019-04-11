package se.tink.backend.aggregation.workers.commands.state;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.commands.LoginAgentWorkerCommand;
import se.tink.backend.aggregation.workers.metrics.TimerCacheLoader;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;

public class LoginAgentWorkerCommandState {

    public static final String LOAD_PERSISTENT_SESSION_TIMER_NAME =
            "load_persistent_session_duration";
    public static final String LOCK_TIMER_NAME = "acquire_lock_duration";
    public static final String RELEASE_LOCK_TIMER_NAME = "release_lock_duration";
    public static final String LOGIN_TIMER_NAME = "login_duration";
    public static final String LOGOOUT_TIMER_NAME = "logout_duration";

    private LoadingCache<MetricId.MetricLabels, Timer> loadPersistentSessionTimer;
    private LoadingCache<MetricId.MetricLabels, Timer> lockTimer;
    private LoadingCache<MetricId.MetricLabels, Timer> releaseLockTimer;
    private LoadingCache<MetricId.MetricLabels, Timer> loginTimer;
    private LoadingCache<MetricId.MetricLabels, Timer> logoutTimer;

    @Inject
    public LoginAgentWorkerCommandState(MetricRegistry metricRegistry) {
        CacheBuilder<Object, Object> cacheBuilder = CacheBuilder.newBuilder();

        loadPersistentSessionTimer =
                cacheBuilder.build(
                        new TimerCacheLoader(metricRegistry, LOAD_PERSISTENT_SESSION_TIMER_NAME));
        lockTimer = cacheBuilder.build(new TimerCacheLoader(metricRegistry, LOCK_TIMER_NAME));
        releaseLockTimer =
                cacheBuilder.build(new TimerCacheLoader(metricRegistry, RELEASE_LOCK_TIMER_NAME));
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

    public ArrayList<Timer.Context> getTimerContexts(
            String metric, CredentialsTypes credentialsTypes) throws ExecutionException {
        MetricId.MetricLabels globalName =
                new MetricId.MetricLabels()
                        .add("class", LoginAgentWorkerCommand.class.getSimpleName())
                        .add("credential_type", "global")
                        .add(
                                "command",
                                AgentWorkerOperationMetricType.EXECUTE_COMMAND.getMetricName());

        MetricId.MetricLabels typeName =
                new MetricId.MetricLabels()
                        .add("class", LoginAgentWorkerCommand.class.getSimpleName())
                        .add("credential_type", credentialsTypes.name().toLowerCase())
                        .add(
                                "command",
                                AgentWorkerOperationMetricType.EXECUTE_COMMAND.getMetricName());

        switch (metric) {
            case LOCK_TIMER_NAME:
                return Lists.newArrayList(
                        getLockTimers().get(typeName).time(),
                        getLockTimers().get(globalName).time());
            case RELEASE_LOCK_TIMER_NAME:
                return Lists.newArrayList(
                        getReleaseLockTimers().get(typeName).time(),
                        getReleaseLockTimers().get(globalName).time());
            case LOGIN_TIMER_NAME:
                return Lists.newArrayList(
                        getLoginTimers().get(typeName).time(),
                        getLoginTimers().get(globalName).time());
            case LOAD_PERSISTENT_SESSION_TIMER_NAME:
                return Lists.newArrayList(
                        getLoadPersistentSessionTimers().get(typeName).time(),
                        getLoadPersistentSessionTimers().get(globalName).time());
            case LOGOOUT_TIMER_NAME:
                typeName =
                        new MetricId.MetricLabels()
                                .add("class", LoginAgentWorkerCommand.class.getSimpleName())
                                .add("credential_type", credentialsTypes.name().toLowerCase())
                                .add(
                                        "command",
                                        AgentWorkerOperationMetricType.POST_PROCESS_COMMAND
                                                .getMetricName());
                return Lists.newArrayList(
                        getLogoutTimers().get(typeName).time(),
                        getLogoutTimers().get(globalName).time());
        }
        return Lists.newArrayList();
    }
}
