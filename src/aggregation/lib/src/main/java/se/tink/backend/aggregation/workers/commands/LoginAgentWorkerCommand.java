package se.tink.backend.aggregation.workers.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;
import se.tink.backend.aggregation.workers.commands.login.LoginExecutor;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.metrics.MetricActionComposite;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.types.timers.Timer.Context;
import se.tink.libraries.user.rpc.User;

public class LoginAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final AggregationLogger log =
            new AggregationLogger(LoginAgentWorkerCommand.class);

    private static final String LOCK_FORMAT_BANKID_REFRESH =
            "/locks/aggregation/%s/bankid"; // % (userId)
    private CredentialsStatus initialCredentialStatus;

    static class MetricName {
        static final String METRIC = "agent_login";

        static final String IS_LOGGED_IN = "is-logged-in";
        static final String LOGIN = "login";
        static final String LOGIN_MANUAL = "login-manual";
        static final String LOGIN_AUTO = "login-auto";
        static final String LOGIN_CRON = "login-cron";
        static final String LOGOUT = "logout";
        static final String ACQUIRE_LOCK = "acquire-lock";
        static final String RELEASE_LOCK = "release-lock";
        static final String PERSIST_LOGIN_SESSION = "persist-login-session";
    }

    private final AgentWorkerCommandMetricState metrics;
    private final LoginAgentWorkerCommandState state;
    private final AgentWorkerCommandContext context;
    private final StatusUpdater statusUpdater;
    private final Credentials credentials;
    private final User user;
    private Agent agent;
    private final SupplementalInformationController supplementalInformationController;
    private final LoginAgentEventProducer loginAgentEventProducer;

    private InterProcessSemaphoreMutex lock;
    private final long startTime;

    public LoginAgentWorkerCommand(
            AgentWorkerCommandContext context,
            LoginAgentWorkerCommandState state,
            AgentWorkerCommandMetricState metrics,
            LoginAgentEventProducer loginAgentEventProducer) {
        final CredentialsRequest request = context.getRequest();
        this.context = context;
        this.statusUpdater = context;
        this.state = state;
        this.metrics = metrics.init(this);
        this.credentials = request.getCredentials();
        this.user = request.getUser();
        this.supplementalInformationController =
                new SupplementalInformationControllerImpl(context, request.getCredentials());
        this.loginAgentEventProducer = loginAgentEventProducer;
        this.startTime = System.nanoTime();
    }

    @Override
    public String getMetricName() {
        return MetricName.METRIC;
    }

    private void emitLoginResultEvent(LoginResult result) {

        long finishTime = System.nanoTime();
        long elapsedTime = finishTime - startTime;

        loginAgentEventProducer.sendLoginCompletedEvent(
                context.getRequest().getCredentials().getProviderName(),
                context.getCorrelationId(),
                result,
                elapsedTime,
                context.getAppId(),
                context.getClusterId(),
                context.getRequest().getCredentials().getUserId());
    }

    @Override
    public AgentWorkerCommandResult execute() throws Exception {

        log.info(
                String.format(
                        "Credentials contain - supplemental Information: {}: %s",
                        credentials.getSupplementalInformation()));
        log.info(
                String.format(
                        "Credentials contain - status payload: {}: %s",
                        credentials.getStatusPayload()));
        log.info(String.format("Credentials contain - status: {}: %s", credentials.getStatus()));

        agent = context.getAgent();
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        initialCredentialStatus = credentials.getStatus();

        AgentWorkerCommandResult result;

        try {

            if (context.getRequest().getType() == CredentialsRequestType.TRANSFER
                    && credentials.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR) {

                statusUpdater.updateStatus(
                        CredentialsStatus.AUTHENTICATION_ERROR,
                        context.getCatalog()
                                .getString(
                                        "Invalid credentials status. Update the credentials before retrying the operation."));
                result = AgentWorkerCommandResult.ABORT;
                emitLoginResultEvent(LoginResult.CREDENTIALS_NOT_UPDATED_FOR_TRANSFER);
            } else {
                Optional<Boolean> loggedIn = isLoggedIn();
                if (!loggedIn.isPresent()) {
                    result = AgentWorkerCommandResult.ABORT;
                } else if (loggedIn.get()) {
                    result = AgentWorkerCommandResult.CONTINUE;
                } else if (!acquireLock()) {
                    // If this is a BankID credentials, we need to take a lock around the login
                    // method.
                    result = AgentWorkerCommandResult.ABORT;
                    emitLoginResultEvent(LoginResult.COULD_NOT_ACQUIRE_LOCK_FOR_BANKID_LOGIN);
                } else {
                    result = login();
                }
            }
        } finally {
            metrics.stop();
            log.info(
                    String.format(
                            "Credentials contain - supplemental Information: {}: %s",
                            credentials.getSupplementalInformation()));
            log.info(
                    String.format(
                            "Credentials contain - status payload: {}: %s",
                            credentials.getStatusPayload()));
            log.info(
                    String.format("Credentials contain - status: {}: %s", credentials.getStatus()));
        }

        log.info(String.format("Credentials contain - status: {}: %s", credentials.getStatus()));

        return result;
    }

    private MetricId.MetricLabels metricForAction(String action) {
        return new MetricId.MetricLabels().add("action", action);
    }

    public boolean getIsForceAuthenticate() {
        return context.getRequest().isForceAuthenticate();
    }

    private Optional<Boolean> isLoggedIn() throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            log.info("agent is not instanceof PersistentLogin");
            return Optional.of(Boolean.FALSE);
        }

        MetricAction action = metrics.buildAction(metricForAction(MetricName.IS_LOGGED_IN));
        ArrayList<Context> loadPersistentSessionTimerContexts =
                state.getTimerContexts(
                        state.LOAD_PERSISTENT_SESSION_TIMER_NAME, credentials.getType());
        Boolean result = Boolean.FALSE;

        PersistentLogin persistentAgent = (PersistentLogin) agent;

        if (getIsForceAuthenticate()) {
            log.info("Clearing session to force authentication towards the bank");
            persistentAgent.clearLoginSession();
        } else {
            long timeLoadingSession = 0;
            long timeAgentIsLoggedIn = 0;
            try {
                long beforeLoad = System.nanoTime();
                persistentAgent.loadLoginSession();
                timeLoadingSession = System.nanoTime() - beforeLoad;

                long beforeIsLoggedIn = System.nanoTime();
                if (persistentAgent.isLoggedIn()) {
                    timeAgentIsLoggedIn = System.nanoTime() - beforeIsLoggedIn;
                    action.completed();
                    log.info("We're already logged in. Moving along.");
                    emitLoginResultEvent(LoginResult.ALREADY_LOGGED_IN);
                    result = Boolean.TRUE;
                } else {
                    timeAgentIsLoggedIn = System.nanoTime() - beforeIsLoggedIn;
                    action.completed();
                    log.info("We're not logged in. Clear Session and Login in again.");
                    persistentAgent.clearLoginSession();
                }
            } catch (BankServiceException e) {
                log.info(String.format("Bank service exception: %s", e.getMessage()), e);
                action.unavailable();
                statusUpdater.updateStatus(CredentialsStatus.TEMPORARY_ERROR);
                // couldn't determine isLoggedIn or not, return ABORT
                emitLoginResultEvent(
                        LoginResult
                                .CANNOT_DETERMINE_IF_ALREADY_LOGGED_IN_DUE_TO_BANK_SERVICE_ERROR);
                return Optional.empty();
            } catch (Exception e) {
                action.failed();
                emitLoginResultEvent(
                        LoginResult.CANNOT_DETERMINE_IF_ALREADY_LOGGED_IN_DUE_TO_ERROR);
                throw e;
            } finally {
                stopCommandContexts(loadPersistentSessionTimerContexts);
            }
            if (timeAgentIsLoggedIn > 0 && timeLoadingSession > 0) {
                long timeAgentIsLoggedInSeconds =
                        TimeUnit.SECONDS.convert(timeAgentIsLoggedIn, TimeUnit.NANOSECONDS);
                long timeLoadingSessionSeconds =
                        TimeUnit.SECONDS.convert(timeLoadingSession, TimeUnit.NANOSECONDS);
                log.info(
                        String.format(
                                "Time loading session: %d s, time agent isLoggedIn: %d s",
                                timeLoadingSessionSeconds, timeAgentIsLoggedInSeconds));
            }
        }
        return Optional.ofNullable(result);
    }

    private boolean acquireLock() throws Exception {
        if (Objects.equals(credentials.getType(), CredentialsTypes.MOBILE_BANKID)) {
            ArrayList<Context> lockTimerContext =
                    state.getTimerContexts(state.LOCK_TIMER_NAME, credentials.getType());
            MetricAction action = metrics.buildAction(metricForAction(MetricName.ACQUIRE_LOCK));

            try {
                lock =
                        new InterProcessSemaphoreMutex(
                                context.getCoordinationClient(),
                                String.format(LOCK_FORMAT_BANKID_REFRESH, user.getId()));

                if (!lock.acquire(2, TimeUnit.MINUTES)) {
                    statusUpdater.updateStatus(CredentialsStatus.UNCHANGED);
                    log.warn("Login failed due not able to acquire lock");
                    action.failed();
                    return false;
                }

                action.completed();
            } catch (Exception e) {
                action.failed();
                throw e;
            } finally {
                stopCommandContexts(lockTimerContext);
            }
        }

        return true;
    }

    private boolean isManualAuthentication(CredentialsRequest request) {
        ManualOrAutoAuthenticationAgentVisitor visitor =
                new ManualOrAutoAuthenticationAgentVisitor(request);
        agent.accept(visitor);
        log.info("Authentication requires user intervention: " + visitor.isManualAuthentication());
        return visitor.isManualAuthentication();
    }

    private AgentWorkerCommandResult login() throws Exception {
        ArrayList<Context> loginTimerContext =
                state.getTimerContexts(state.LOGIN_TIMER_NAME, credentials.getType());

        try {
            return new LoginExecutor(
                            statusUpdater,
                            context,
                            supplementalInformationController,
                            loginAgentEventProducer,
                            startTime)
                    .executeLogin(agent, createLoginMetricAction(), context.getRequest());
        } finally {
            stopCommandContexts(loginTimerContext);

            // If we have a lock, release it.
            releaseLock();
        }
    }

    private MetricActionIface createLoginMetricAction() {
        final CredentialsRequest request = context.getRequest();
        final boolean isCron = !request.isManual();
        MetricAction action = metrics.buildAction(metricForAction(MetricName.LOGIN));
        MetricAction actionLoginType =
                isCron
                        ? metrics.buildAction(metricForAction(MetricName.LOGIN_CRON))
                        : (isManualAuthentication(request)
                                ? metrics.buildAction(metricForAction(MetricName.LOGIN_MANUAL))
                                : metrics.buildAction(metricForAction(MetricName.LOGIN_AUTO)));
        return new MetricActionComposite(action, actionLoginType);
    }

    private void releaseLock() throws Exception {
        if (lock != null && lock.isAcquiredInThisProcess()) {
            ArrayList<Context> releaseLockTimer =
                    state.getTimerContexts(state.RELEASE_LOCK_TIMER_NAME, credentials.getType());
            MetricAction action = metrics.buildAction(metricForAction(MetricName.RELEASE_LOCK));

            try {
                lock.release();

                action.completed();
            } catch (Exception e) {
                action.failed();
                throw e;
            } finally {
                stopCommandContexts(releaseLockTimer);
            }
        }
    }

    @Override
    public void postProcess() throws Exception {

        // If we did not successfully execute in case when credentials has been just created,
        // there's no point in doing anything here.

        log.info(
                String.format(
                        "Credentials contain - supplemental Information: {}: %s",
                        credentials.getSupplementalInformation()));
        log.info(
                String.format(
                        "Credentials contain - status payload: {}: %s",
                        credentials.getStatusPayload()));
        log.info(String.format("Credentials contain - status: {}: %s", credentials.getStatus()));

        if (agent == null || initialCredentialStatus == CredentialsStatus.CREATED) {
            return;
        }

        metrics.start(AgentWorkerOperationMetricType.POST_PROCESS_COMMAND);

        try {
            if (agent instanceof PersistentLogin) {
                // If the agent support persistent sessions, save the session data and do not log
                // out.
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
        MetricAction action =
                metrics.buildAction(metricForAction(MetricName.PERSIST_LOGIN_SESSION));

        try {
            agent.persistLoginSession();

            action.completed();
        } catch (Exception e) {
            action.failed();
            throw e;
        }
    }

    private void logout() throws Exception {
        ArrayList<Context> logoutTimerContext =
                state.getTimerContexts(state.LOGOOUT_TIMER_NAME, credentials.getType());
        MetricAction action = metrics.buildAction(metricForAction(MetricName.LOGOUT));

        try {
            agent.logout();

            action.completed();
        } catch (Exception e) {
            action.failed();
            throw e;
        } finally {
            stopCommandContexts(logoutTimerContext);
        }
    }

    private void stopCommandContexts(List<Context> contexts) {
        for (Context context : contexts) {
            context.stop();
        }
    }
}
