package se.tink.backend.aggregation.workers.commands.login;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.workers.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.AgentWorkerCommandResult;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResultReason;

public class LoginExecutor {

    private List<LoginHandler> loginHandlerChain = new LinkedList<>();
    private List<LoginExceptionHandler> loginExceptionHandlerChain = new LinkedList<>();
    private final LoginAgentEventProducer loginAgentEventProducer;
    private long loginWorkerStartTimestamp;
    private AgentWorkerCommandContext context;

    private static final ImmutableMap<LoginError, LoginResultReason> loginErrorMapper =
            ImmutableMap.<LoginError, LoginResultReason>builder()
                    .put(LoginError.NOT_CUSTOMER, LoginResultReason.LOGIN_ERROR_NOT_CUSTOMER)
                    .put(LoginError.NOT_SUPPORTED, LoginResultReason.LOGIN_ERROR_NOT_SUPPORTED)
                    .put(
                            LoginError.INCORRECT_CREDENTIALS,
                            LoginResultReason.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                    .put(
                            LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT,
                            LoginResultReason.LOGIN_ERROR_INCORRECT_CREDENTIALS_LAST_ATTEMPT)
                    .put(
                            LoginError.INCORRECT_CHALLENGE_RESPONSE,
                            LoginResultReason.LOGIN_ERROR_INCORRECT_CHALLENGE_RESPONSE)
                    .put(
                            LoginError.CREDENTIALS_VERIFICATION_ERROR,
                            LoginResultReason.LOGIN_ERROR_CREDENTIALS_VERIFICATION_ERROR)
                    .put(
                            LoginError.WRONG_PHONENUMBER,
                            LoginResultReason.LOGIN_ERROR_WRONG_PHONE_NUMBER)
                    .put(
                            LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE,
                            LoginResultReason.LOGIN_ERROR_WRONG_PHONE_NUMBER_OR_INACTIVATED_SERVICE)
                    .put(
                            LoginError.ERROR_WITH_MOBILE_OPERATOR,
                            LoginResultReason.LOGIN_ERROR_ERROR_WITH_MOBILE_OPERATOR)
                    .put(
                            LoginError.REGISTER_DEVICE_ERROR,
                            LoginResultReason.LOGIN_ERROR_REGISTER_DEVICE_ERROR)
                    .put(
                            LoginError.NO_ACCESS_TO_MOBILE_BANKING,
                            LoginResultReason.LOGIN_ERROR_NO_ACCESS_TO_MOBILE_BANKING)
                    .put(
                            LoginError.PASSWORD_CHANGED,
                            LoginResultReason.LOGIN_ERROR_PASSWORD_CHANGED)
                    .build();

    public LoginExecutor(
            final StatusUpdater statusUpdater,
            final AgentWorkerCommandContext context,
            final SupplementalInformationController supplementalInformationController,
            final LoginAgentEventProducer loginAgentEventProducer,
            final long loginWorkerStartTimestamp) {
        initLoginHandlerChain(supplementalInformationController);
        initLoginExceptionHandlerChain(statusUpdater, context);
        this.loginAgentEventProducer = loginAgentEventProducer;
        this.loginWorkerStartTimestamp = loginWorkerStartTimestamp;
        this.context = context;
    }

    private void emitLoginResultEvent(LoginResultReason reason) {

        long finishTime = System.nanoTime();
        long elapsedTime = finishTime - this.loginWorkerStartTimestamp;

        loginAgentEventProducer.sendLoginCompletedEvent(
                context.getRequest().getCredentials().getProviderName(),
                context.getCorrelationId(),
                reason,
                elapsedTime,
                context.getAppId(),
                context.getClusterId(),
                context.getRequest().getCredentials().getUserId());
    }

    private void initLoginHandlerChain(
            final SupplementalInformationController supplementalInformationController) {
        loginHandlerChain.add(
                new ProgressiveAuthenticatorLoginHandler(supplementalInformationController));
        loginHandlerChain.add(new DefaultAgentLoginController());
        loginHandlerChain.add(new LoginFailedHandler());
    }

    private void initLoginExceptionHandlerChain(
            final StatusUpdater statusUpdater, final AgentWorkerCommandContext context) {
        loginExceptionHandlerChain.add(new BankIdLoginExceptionHandler(statusUpdater, context));
        loginExceptionHandlerChain.add(
                new BankServiceLoginExceptionHandler(statusUpdater, context));
        loginExceptionHandlerChain.add(
                new AuthenticationAndAuthorizationLoginExceptionHandler(statusUpdater, context));
        loginExceptionHandlerChain.add(new DefaultLoginExceptionHandler(statusUpdater, context));
    }

    public AgentWorkerCommandResult executeLogin(
            final Agent agent,
            final MetricActionIface metricAction,
            final Credentials credentials) {
        try {
            return processLoginHandlerChain(agent, metricAction, credentials);
        } catch (Exception ex) {
            return processLoginExceptionHandlerChain(ex, metricAction);
        }
    }

    private AgentWorkerCommandResult processLoginHandlerChain(
            final Agent agent, final MetricActionIface metricAction, final Credentials credentials)
            throws Exception {
        for (LoginHandler loginHandler : loginHandlerChain) {
            Optional<AgentWorkerCommandResult> result =
                    loginHandler.handleLogin(agent, metricAction, credentials);
            if (result.isPresent()) {
                switch (result.get()) {
                    case CONTINUE:
                        emitLoginResultEvent(LoginResultReason.SUCCESSFUL_LOGIN);
                        break;
                    case ABORT:
                        // TODO: Send a dedicated event here (USER_ABORT)
                        break;
                    case REJECT:
                        // Should not come here...
                        break;
                }
                return result.get();
            }
        }
        throw new IllegalStateException("There is no more login handlers to process");
    }

    private AgentWorkerCommandResult processLoginExceptionHandlerChain(
            final Exception ex, final MetricActionIface metricAction) {

        // We emit proper login event that contains the proper reason for the error here
        if (ex instanceof LoginException) {
            LoginError error = ((LoginException) ex).getError();
            LoginResultReason reason = loginErrorMapper.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResultReason.LOGIN_ERROR_UNKNOWN);
        }

        for (LoginExceptionHandler loginExceptionHandler : loginExceptionHandlerChain) {
            Optional<AgentWorkerCommandResult> result =
                    loginExceptionHandler.handleLoginException(ex, metricAction);
            if (result.isPresent()) {
                return result.get();
            }
        }
        throw new IllegalStateException("There is no more login exception handlers to process");
    }
}
