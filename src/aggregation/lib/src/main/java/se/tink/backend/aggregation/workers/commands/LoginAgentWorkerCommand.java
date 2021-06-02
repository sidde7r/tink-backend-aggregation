package se.tink.backend.aggregation.workers.commands;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.PersistentLogin;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticationExecutor;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.events.IntegrationParameters;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationControllerImpl;
import se.tink.backend.aggregation.workers.commands.login.AgentLoginCompletedEventUserInteractionInformationProvider;
import se.tink.backend.aggregation.workers.commands.login.DataStudioLoginEventPublisherService;
import se.tink.backend.aggregation.workers.commands.login.LoginExecutor;
import se.tink.backend.aggregation.workers.commands.login.MetricsFactory;
import se.tink.backend.aggregation.workers.commands.metrics.MetricsCommand;
import se.tink.backend.aggregation.workers.commands.state.LoginAgentWorkerCommandState;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.AgentWorkerCommandMetricState;
import se.tink.backend.aggregation.workers.metrics.MetricAction;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommand;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.operation.type.AgentWorkerOperationMetricType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsRequestType;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.histograms.Histogram;
import se.tink.libraries.metrics.types.timers.Timer.Context;
import se.tink.libraries.user.rpc.User;
import src.libraries.connectivity_errors.ConnectivityErrorFactory;

@Slf4j
public class LoginAgentWorkerCommand extends AgentWorkerCommand implements MetricsCommand {

    private static final String LOCK_FORMAT_BANKID_REFRESH =
            "/locks/aggregation/%s/bankid"; // % (userId)

    static class MetricName {
        static final String METRIC = "agent_login";
        static final String LOGIN_SESSION_EXPIRY_METRIC = "agent_login_session_lifetime";

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

    private static final List<Integer> SESSION_LIFETIME_BUCKETS =
            Arrays.asList(1, 2, 3, 5, 8, 13, 21, 30, 60, 90);
    private CredentialsStatus initialCredentialStatus;
    private final AgentWorkerCommandMetricState metrics;
    private final LoginAgentWorkerCommandState state;
    private final AgentWorkerCommandContext context;
    private final StatusUpdater statusUpdater;
    private final Credentials credentials;
    private final User user;
    private Agent agent;
    private final SupplementalInformationController supplementalInformationController;
    private final LoginAgentEventProducer loginAgentEventProducer;
    private final MetricRegistry metricRegistry;

    private InterProcessSemaphoreMutex lock;
    private final long startTime;

    public LoginAgentWorkerCommand(
            AgentWorkerCommandContext context,
            LoginAgentWorkerCommandState state,
            AgentWorkerCommandMetricState metrics,
            MetricRegistry metricRegistry,
            LoginAgentEventProducer loginAgentEventProducer) {
        this.metricRegistry = metricRegistry;
        final CredentialsRequest request = context.getRequest();
        this.context = context;
        this.statusUpdater = context;
        this.state = state;
        this.credentials = request.getCredentials();
        this.user = request.getUser();
        this.supplementalInformationController =
                new SupplementalInformationControllerImpl(
                        context,
                        request.getCredentials(),
                        request.getState(),
                        request.getProvider().getClassName());
        this.loginAgentEventProducer = loginAgentEventProducer;
        this.startTime = System.nanoTime();
        this.metrics =
                metrics.init(
                        this,
                        MetricId.MetricLabels.from(
                                ImmutableMap.of("provider", request.getProvider().getName())));
    }

    @Override
    public String getMetricName() {
        return MetricName.METRIC;
    }

    private void emitLoginResultEvent(LoginResult result) {

        long finishTime = System.nanoTime();
        long elapsedTime = finishTime - startTime;

        loginAgentEventProducer.sendLoginCompletedEvent(
                IntegrationParameters.builder()
                        .providerName(context.getRequest().getCredentials().getProviderName())
                        .correlationId(context.getCorrelationId())
                        .appId(context.getAppId())
                        .clusterId(context.getClusterId())
                        .userId(context.getRequest().getCredentials().getUserId())
                        .build(),
                result,
                elapsedTime,
                AgentLoginCompletedEventUserInteractionInformationProvider
                        .userInteractionInformation(
                                context.getSupplementalInteractionCounter(), context.getRequest()));
    }

    @Override
    protected AgentWorkerCommandResult doExecute() throws Exception {

        CredentialsStatusInfoUtlis.logCredentialsInfo(credentials);

        agent = context.getAgent();
        metrics.start(AgentWorkerOperationMetricType.EXECUTE_COMMAND);
        initialCredentialStatus = credentials.getStatus();
        Date initialCredentialsSessionExpiryDate = credentials.getSessionExpiryDate();

        AgentWorkerCommandResult result;

        try {

            if (context.getRequest().getType() == CredentialsRequestType.TRANSFER
                    && credentials.getStatus() == CredentialsStatus.AUTHENTICATION_ERROR) {

                statusUpdater.updateStatus(
                        CredentialsStatus.UNCHANGED,
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

            if (result == AgentWorkerCommandResult.CONTINUE) {
                emitSessionExpiryMertic(initialCredentialsSessionExpiryDate);
            }
        } finally {
            metrics.stop();

            CredentialsStatusInfoUtlis.logCredentialsInfo(credentials);
        }

        log.info(
                "[LOG IN] AgentWorkerCommandResult: {}, credentialsId: {}",
                Optional.ofNullable(result).map(Enum::toString).orElse(null),
                credentials.getId());
        return result;
    }

    private void emitSessionExpiryMertic(Date initialCredentialsSessionExpiryDate) {
        Optional<Date> sessionExpiryDate = Optional.ofNullable(credentials.getSessionExpiryDate());

        boolean hasChanged =
                !Objects.equals(
                        sessionExpiryDate.orElse(null), initialCredentialsSessionExpiryDate);

        log.info(
                String.format(
                        "[LOG IN] LoginAgentWorkerCommand, session expiry details: exists=%b, hasChanged=%b",
                        sessionExpiryDate.isPresent(), hasChanged));
        Histogram histogram =
                histogram(
                        MetricId.newId(MetricName.LOGIN_SESSION_EXPIRY_METRIC)
                                .label(
                                        new MetricId.MetricLabels()
                                                .add(
                                                        "session_expiry_date_exists",
                                                        String.valueOf(
                                                                sessionExpiryDate.isPresent()))
                                                .add(
                                                        "session_expiry_date_changed",
                                                        String.valueOf(hasChanged))));

        Optional<Long> sessionLifetime =
                sessionExpiryDate
                        .map(d -> d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
                        .map(localDate -> ChronoUnit.DAYS.between(LocalDate.now(), localDate));

        histogram.update(sessionLifetime.orElse(0L));
    }

    private MetricId.MetricLabels metricForAction(String action) {
        return new MetricId.MetricLabels().add("action", action);
    }

    private Optional<Boolean> isLoggedIn() throws Exception {
        if (!(agent instanceof PersistentLogin)) {
            log.info("[LOG IN] agent is not instanceof PersistentLogin");
            return Optional.of(Boolean.FALSE);
        }

        MetricAction action = metrics.buildAction(metricForAction(MetricName.IS_LOGGED_IN));
        ArrayList<Context> loadPersistentSessionTimerContexts =
                state.getTimerContexts(
                        state.LOAD_PERSISTENT_SESSION_TIMER_NAME, credentials.getType());
        Boolean result = Boolean.FALSE;

        PersistentLogin persistentAgent = (PersistentLogin) agent;

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
                log.info("[LOG IN] Already logged in.");
                emitLoginResultEvent(LoginResult.ALREADY_LOGGED_IN);
                result = Boolean.TRUE;
            } else {
                timeAgentIsLoggedIn = System.nanoTime() - beforeIsLoggedIn;
                action.completed();
                log.info("[LOG IN] Not logged in. Clearing session and trying to log in again");
                persistentAgent.clearLoginSession();
            }
        } catch (BankServiceException e) {
            log.info("[LOG IN] Failed with error: {}", e.getMessage(), e);
            action.unavailable();
            ConnectivityError error = ConnectivityErrorFactory.fromLegacy(e);
            statusUpdater.updateStatusWithError(CredentialsStatus.TEMPORARY_ERROR, null, error);
            // couldn't determine isLoggedIn or not, return ABORT
            emitLoginResultEvent(
                    LoginResult.CANNOT_DETERMINE_IF_ALREADY_LOGGED_IN_DUE_TO_BANK_SERVICE_ERROR);
            return Optional.empty();
        } catch (Exception e) {
            action.failed();
            emitLoginResultEvent(LoginResult.CANNOT_DETERMINE_IF_ALREADY_LOGGED_IN_DUE_TO_ERROR);
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
                    "[LOG IN] Loaded session in {} s, Login command completed in {} s",
                    timeLoadingSessionSeconds,
                    timeAgentIsLoggedInSeconds);
        }
        log.info(
                "[LOG IN] User is logged in {} with credentialsId: {}",
                result,
                credentials.getId());
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
                    ConnectivityError error =
                            ConnectivityErrorFactory.tinkSideError(
                                    ConnectivityErrorDetails.TinkSideErrors
                                            .TINK_INTERNAL_SERVER_ERROR);
                    statusUpdater.updateStatusWithError(CredentialsStatus.UNCHANGED, null, error);
                    log.warn("[LOG IN] Login failed due not able to acquire lock");
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

    private AgentWorkerCommandResult login() throws Exception {
        ArrayList<Context> loginTimerContext =
                state.getTimerContexts(state.LOGIN_TIMER_NAME, credentials.getType());

        try {
            return new LoginExecutor(
                            new MetricsFactory(metrics),
                            statusUpdater,
                            new AgentPlatformAuthenticationExecutor())
                    .execute(
                            context,
                            supplementalInformationController,
                            new DataStudioLoginEventPublisherService(
                                    loginAgentEventProducer, startTime, context));

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
    protected void doPostProcess() throws Exception {

        // If we did not successfully execute in case when credentials has been just created,
        // there's no point in doing anything here.

        CredentialsStatusInfoUtlis.logCredentialsInfo(credentials);
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

    private void persistLoginSession(PersistentLogin agent) {
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

    private Histogram histogram(MetricId metricId) {
        CredentialsRequest request = context.getRequest();
        Provider provider = request.getProvider();
        metricId =
                metricId.label("provider_name", provider.getName())
                        .label("provider_type", provider.getMetricTypeName())
                        .label("provider_access_type", provider.getAccessType().name())
                        .label("market", provider.getMarket())
                        .label("className", provider.getClassName())
                        .label("credential", credentials.getMetricTypeName())
                        .label("request_type", request.getType().name());
        return metricRegistry.histogram(metricId, SESSION_LIFETIME_BUCKETS);
    }
}
