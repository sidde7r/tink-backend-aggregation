package se.tink.backend.aggregation.workers.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsStatus;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.annotations.ProgressiveAuth;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.AgentWorkerOperationMetricType;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequestType;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.Timer.Context;
import se.tink.libraries.user.rpc.User;

public class LoginAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {
    private static final AggregationLogger log =
            new AggregationLogger(LoginAgentWorkerCommand.class);

    private static final String LOCK_FORMAT_BANKID_REFRESH =
            "/locks/aggregation/%s/bankid"; // % (userId)
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

    private final AgentWorkerCommandMetricState metrics;
    private final LoginAgentWorkerCommandState state;
    private final AgentWorkerCommandContext context;
    private final StatusUpdater statusUpdater;
    private final Credentials credentials;
    private final User user;
    private Agent agent;
    private final SupplementalInformationController supplementalInformationController;

    private InterProcessSemaphoreMutex lock;

    public LoginAgentWorkerCommand(
            AgentWorkerCommandContext context,
            LoginAgentWorkerCommandState state,
            AgentWorkerCommandMetricState metrics) {
        final CredentialsRequest request = context.getRequest();
        this.context = context;
        this.statusUpdater = context;
        this.state = state;
        this.metrics = metrics.init(this);
        this.credentials = request.getCredentials();
        this.user = request.getUser();
        this.supplementalInformationController =
                new SupplementalInformationController(context, request.getCredentials());
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
            if (context.getRequest().getType() == CredentialsRequestType.TRANSFER
                    && credentials.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR) {

                statusUpdater.updateStatus(
                        CredentialsStatus.AUTHENTICATION_ERROR,
                        context.getCatalog()
                                .getString(
                                        "Invalid credentials status. Update the credentials before retrying the operation."));
                result = AgentWorkerCommandResult.ABORT;

            } else if (isLoggedIn()) {
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
        return new MetricId.MetricLabels().add("action", action);
    }

    private boolean isLoggedIn() throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            return false;
        }

        MetricAction action = metrics.buildAction(metricForAction(MetricName.IS_LOGGED_IN));
        ArrayList<Context> loadPersistentSessionTimerContexts =
                state.getTimerContexts(
                        state.LOAD_PERSISTENT_SESSION_TIMER_NAME, credentials.getType());

        PersistentLogin persistentAgent = (PersistentLogin) agent;

        try {
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
            stopCommandContexts(loadPersistentSessionTimerContexts);
        }

        return false;
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

    private void progressiveLogin() throws Exception {
        AuthenticationResponse response =
                ((ProgressiveAuthAgent) agent)
                        .login(
                                new AuthenticationRequest(
                                        AuthenticationStepConstants.STEP_INIT, null));
        while (!AuthenticationStepConstants.STEP_FINALIZE.equals(response.getStep())) {
            // TODO auth: think about cases other than supplemental info, e.g. bankid, redirect
            // etc.
            List<Field> fields = response.getFields();
            Map<String, String> map =
                    supplementalInformationController.askSupplementalInformation(
                            fields.toArray(new Field[fields.size()]));
            log.info(String.format("progressiveLogin - %s", map));
            response =
                    ((ProgressiveAuthAgent) agent)
                            .login(
                                    new AuthenticationRequest(
                                            response.getStep(), new ArrayList<>(map.values())));
        }
    }

    private AgentWorkerCommandResult login() throws Exception {
        ArrayList<Context> loginTimerContext =
                state.getTimerContexts(state.LOGIN_TIMER_NAME, credentials.getType());
        MetricAction action = metrics.buildAction(metricForAction(MetricName.LOGIN));

        try {
            // TODO auth: temporarily use the annotation to filter agents that are migrated to use
            // new Auth flow
            if (agent.getAgentClass().getAnnotation(ProgressiveAuth.class) != null) {
                progressiveLogin();
                action.completed();
                return AgentWorkerCommandResult.CONTINUE;
            } else if (agent.login()) {
                action.completed();
                return AgentWorkerCommandResult.CONTINUE;
            } else {
                log.warn("Login failed due to agent.login() returned false");

                action.failed();
                return AgentWorkerCommandResult.ABORT;
            }
        } catch (BankIdException e) {
            // The way frontend works now the message will not be displayed to the user.
            statusUpdater.updateStatus(
                    CredentialsStatus.UNCHANGED,
                    context.getCatalog().getString(e.getUserMessage()));
            action.cancelled();
            return AgentWorkerCommandResult.ABORT;
        } catch (BankServiceException e) {
            statusUpdater.updateStatus(
                    CredentialsStatus.TEMPORARY_ERROR,
                    context.getCatalog().getString(e.getUserMessage()));
            action.unavailable();
            return AgentWorkerCommandResult.ABORT;
        } catch (AuthenticationException | AuthorizationException e) {
            statusUpdater.updateStatus(
                    CredentialsStatus.AUTHENTICATION_ERROR,
                    context.getCatalog().getString(e.getUserMessage()));
            action.cancelled();
            return AgentWorkerCommandResult.ABORT;

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            statusUpdater.updateStatus(CredentialsStatus.TEMPORARY_ERROR);
            action.failed();
            return AgentWorkerCommandResult.ABORT;

        } finally {
            stopCommandContexts(loginTimerContext);

            // If we have a lock, release it.
            releaseLock();
        }
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
        // If we did not successfully execute, there's no point in doing anything here.
        if (!weHavePreviouslyLoggedInSuccessfully || agent == null) {
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
