package se.tink.backend.aggregation.workers.commands;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsRequest;
import se.tink.backend.aggregation.rpc.CredentialsStatus;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.aggregation.rpc.User;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerContext;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.TimerCacheLoader;
import se.tink.backend.common.ServiceContext;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.metrics.Timer;
import se.tink.libraries.metrics.Timer.Context;

public class LoginAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final AggregationLogger log = new AggregationLogger(LoginAgentWorkerCommand.class);

    private static final String LOCK_FORMAT_BANKID_REFRESH = "/locks/aggregation/%s/bankid"; // % (userId)
    private boolean weHavePreviouslyLoggedInSuccessfully = false;

    private static class MetricName {
        private static final String METRIC = "agent_login";

        private static final String IS_LOGGED_IN = "is-logged-in";
        private static final String LOGIN = "login";
        private static final String LOGOUT = "logout";
        private static final String ACQUIRE_LOCK = "acquire-lock";
        private static final String RELEASE_LOCK = "release-lock";
        private static final String PERSIST_LOGIN_SESSION = "persist-login-session";
    }

    private static class MetricBuckets {
        private static final ImmutableList<Double> LOGIN_PASSWORD = ImmutableList.of(0.0, .025, .05, .1, .2, 0.4, 0.8, 1.6, 3.2, 6.4);
        private static final ImmutableList<Integer> LOGIN_BANKID = ImmutableList.of(0, 2, 4, 8, 16, 32, 64, 128, 256);
        private static final ImmutableList<Double> ACQUIRE_LOCK = ImmutableList.of(0.0, .025, .05, .1, .2, 0.4, 0.8, 1.6, 3.2, 6.4);
    }

    private static final String LOAD_PERSISTENT_SESSION_TIMER_NAME = "load_persistent_session_duration";
    private static final String LOCK_TIMER_NAME = "acquire_lock_duration";
    private static final String RELEASE_LOCK_TIMER_NAME = "release_lock_duration";
    private static final String LOGIN_TIMER_NAME = "login_duration";
    private static final String LOGOOUT_TIMER_NAME = "logout_duration";

    public static class LoginAgentWorkerCommandState {

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

        private LoadingCache<MetricId.MetricLabels, Timer> getLockTimers() {
            return lockTimer;
        }

        private LoadingCache<MetricId.MetricLabels, Timer> getReleaseLockTimers() {
            return releaseLockTimer;
        }

        private LoadingCache<MetricId.MetricLabels, Timer> getLoginTimers() {
            return loginTimer;
        }

        private LoadingCache<MetricId.MetricLabels, Timer> getLogoutTimers() {
            return logoutTimer;
        }

        private LoadingCache<MetricId.MetricLabels, Timer> getLoadPersistentSessionTimers() {
            return loadPersistentSessionTimer;
        }
    }

    private final AgentWorkerCommandMetricState metrics;
    private final LoginAgentWorkerCommandState state;
    private final AgentWorkerContext context;
    private final Credentials credentials;
    private final User user;
    private Agent agent;

    private InterProcessSemaphoreMutex lock;

    public LoginAgentWorkerCommand(AgentWorkerContext context, LoginAgentWorkerCommandState state,
            AgentWorkerCommandMetricState metrics) {
        final CredentialsRequest request = context.getRequest();
        this.context = context;
        this.state = state;
        this.metrics = metrics.init(this);
        this.credentials = request.getCredentials();
        this.user = request.getUser();
    }

    @Override
    public String getMetricName() {
        return MetricName.METRIC;
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {
        agent = context.getAgent();
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);

        AgentWorkerCommandResult result;
        try {
            if (isLoggedIn()) {
                result = AgentWorkerCommandResult.CONTINUE;
            } else if (!acquireLock()) {
                // If this is a BankID credentials, we need to take a lock around the login method.
                result = AgentWorkerCommandResult.ABORT;
            } else {
                result = login();
            }
        } finally {
            metrics.stop();
        }

        if (Objects.equals(result, AgentWorkerCommandResult.CONTINUE)) {
            weHavePreviouslyLoggedInSuccessfully = true;
        }

        return result;
    }

    private MetricId.MetricLabels metricForAction(String action) {
        return new MetricId.MetricLabels()
                .add("action", action);
    }

    private boolean isLoggedIn() throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            return false;
        }

        MetricAction action = metrics.buildAction(metricForAction(MetricName.IS_LOGGED_IN));
        ArrayList<Context> loadPersistentSessionTimerContexts = getTimerContexts(LOAD_PERSISTENT_SESSION_TIMER_NAME);

        PersistentLogin persistentAgent = (PersistentLogin) agent;

        try {
            action.start();
            persistentAgent.loadLoginSession();

            if (persistentAgent.isLoggedIn()) {
                action.completed();
                log.info("We're already logged in. Moving along.");

                return true;
            } else {
                action.completed();
                log.debug("We're not logged in. Clear Session and Login in again.");

                persistentAgent.clearLoginSession();
            }
        } catch (Exception e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
            stopCommandContexts(loadPersistentSessionTimerContexts);
        }

        return false;
    }

    private boolean acquireLock() throws Exception {
        if (Objects.equals(credentials.getType(), CredentialsTypes.MOBILE_BANKID)) {
            ArrayList<Context> lockTimerContext = getTimerContexts(LOCK_TIMER_NAME);
            MetricAction action = metrics.buildAction(metricForAction(MetricName.ACQUIRE_LOCK));

            try {
                action.start(MetricBuckets.ACQUIRE_LOCK);
                lock = new InterProcessSemaphoreMutex(context.getCoordinationClient(), String.format(
                        LOCK_FORMAT_BANKID_REFRESH, user.getId()));

                if (!lock.acquire(2, TimeUnit.MINUTES)) {
                    context.updateStatus(CredentialsStatus.UNCHANGED);
                    log.warn("Login failed due not able to acquire lock");
                    action.failed();

                    return false;
                }

                action.completed();
            } catch(Exception e) {
                action.failed();
                throw e;
            } finally {
                action.stop();
                stopCommandContexts(lockTimerContext);
            }
        }

        return true;
    }

    private AgentWorkerCommandResult login() throws Exception {
        ArrayList<Context> loginTimerContext = getTimerContexts(LOGIN_TIMER_NAME);
        MetricAction action = metrics.buildAction(metricForAction(MetricName.LOGIN));

        try {
            action.start(credentials.getType() == CredentialsTypes.MOBILE_BANKID ?
                    MetricBuckets.LOGIN_BANKID : MetricBuckets.LOGIN_PASSWORD);

            if (agent.login()) {
                action.completed();
                return AgentWorkerCommandResult.CONTINUE;
            } else {
                log.warn("Login failed due to agent.login() returned false");

                action.failed();
                return AgentWorkerCommandResult.ABORT;
            }
        } catch(BankIdException e) {
            // The way frontend works now the message will not be displayed to the user.
            context.updateStatus(CredentialsStatus.UNCHANGED, context.getCatalog().getString(e.getUserMessage()));
            action.cancelled();
            return AgentWorkerCommandResult.ABORT;
        } catch(BankServiceException e) {
            // The way frontend works now the message will not be displayed to the user.
            context.updateStatus(CredentialsStatus.UNCHANGED, context.getCatalog().getString(e.getUserMessage()));
            action.unavailable();
            return AgentWorkerCommandResult.ABORT;
        } catch (AuthenticationException | AuthorizationException e) {
            context.updateStatus(CredentialsStatus.AUTHENTICATION_ERROR,
                    context.getCatalog().getString(e.getUserMessage()));
            action.cancelled();
            return AgentWorkerCommandResult.ABORT;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            context.updateStatus(CredentialsStatus.TEMPORARY_ERROR);
            action.failed();
            return AgentWorkerCommandResult.ABORT;

        } finally {
            action.stop();
            stopCommandContexts(loginTimerContext);

            // If we have a lock, release it.
            releaseLock();
        }
    }

    private void releaseLock() throws Exception {
        if (lock != null && lock.isAcquiredInThisProcess()) {
            ArrayList<Context> releaseLockTimer = getTimerContexts(RELEASE_LOCK_TIMER_NAME);
            MetricAction action = metrics.buildAction(metricForAction(MetricName.RELEASE_LOCK));

            try {
                action.start();
                lock.release();

                action.completed();
            } catch (Exception e) {
                action.failed();
                throw e;
            } finally {
                action.stop();
                stopCommandContexts(releaseLockTimer);
            }
        }
    }

    @Override
    public void postProcess() throws Exception {
        // If we did not successfully execute, there's no point in doing anything here.
        if (!weHavePreviouslyLoggedInSuccessfully || agent == null) {
            return;
        }

        metrics.start(AgentWorkerOperationMetricType.POST_PROCESS_COMMAND);

        try {
            if (agent instanceof PersistentLogin) {
                // If the agent support persistent sessions, save the session data and do not log out.
                // Only persist for flagged users.
                persistLoginSession((PersistentLogin) agent);
            } else {
                logout();
            }

        } finally {
            agent.close();
            metrics.stop();
        }
    }

    private void persistLoginSession(PersistentLogin agent) throws Exception {
        MetricAction action = metrics.buildAction(metricForAction(MetricName.PERSIST_LOGIN_SESSION));

        try {
            action.start();
            agent.persistLoginSession();

            action.completed();
        } catch(Exception e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
        }
    }

    private void logout() throws Exception {
        ArrayList<Context> logoutTimerContext = getTimerContexts(LOGOOUT_TIMER_NAME);
        MetricAction action = metrics.buildAction(metricForAction(MetricName.LOGOUT));

        try {
            action.start();
            agent.logout();

            action.completed();
        } catch (Exception e) {
            action.failed();
            throw e;
        } finally {
            action.stop();
            stopCommandContexts(logoutTimerContext);
        }
    }

    private ArrayList<Context> getTimerContexts(String metric) throws ExecutionException {
        MetricId.MetricLabels globalName = new MetricId.MetricLabels()
                .add("class", LoginAgentWorkerCommand.class.getSimpleName())
                .add("credential_type", "global")
                .add("command", AgentWorkerOperationMetricType.EXECUTE_COMMAND.getMetricName());

        MetricId.MetricLabels typeName = new MetricId.MetricLabels()
                .add("class", LoginAgentWorkerCommand.class.getSimpleName())
                .add("credential_type", credentials.getType().name().toLowerCase())
                .add("command", AgentWorkerOperationMetricType.EXECUTE_COMMAND.getMetricName());

        switch (metric) {
        case LOCK_TIMER_NAME:
            return Lists.newArrayList(state.getLockTimers().get(typeName).time(),
                    state.getLockTimers().get(globalName).time());
        case RELEASE_LOCK_TIMER_NAME:
            return Lists.newArrayList(state.getReleaseLockTimers().get(typeName).time(), state
                    .getReleaseLockTimers().get(globalName).time());
        case LOGIN_TIMER_NAME:
            return Lists.newArrayList(state.getLoginTimers().get(typeName).time(), state
                    .getLoginTimers().get(globalName).time());
        case LOAD_PERSISTENT_SESSION_TIMER_NAME:
            return Lists.newArrayList(state.getLoadPersistentSessionTimers().get(typeName).time(), state
                    .getLoadPersistentSessionTimers().get(globalName).time());
        case LOGOOUT_TIMER_NAME:
            typeName = new MetricId.MetricLabels()
                    .add("class", LoginAgentWorkerCommand.class.getSimpleName())
                    .add("credential_type", credentials.getType().name().toLowerCase())
                    .add("command", AgentWorkerOperationMetricType.POST_PROCESS_COMMAND.getMetricName());
            return Lists.newArrayList(state.getLogoutTimers().get(typeName).time(), state
                    .getLogoutTimers().get(globalName).time());
        }
        return Lists.newArrayList();
    }

    private void stopCommandContexts(List<Context> contexts) {
        for (Context context : contexts) {
            context.stop();
        }
    }
}
