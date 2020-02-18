package se.tink.backend.aggregation.workers.commands.login;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
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

    private static final ImmutableMap<LoginError, LoginResultReason> LOGIN_ERROR_MAPPER =
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

    private static final ImmutableMap<BankIdError, LoginResultReason> BANKID_ERROR_MAPPER =
            ImmutableMap.<BankIdError, LoginResultReason>builder()
                    .put(BankIdError.CANCELLED, LoginResultReason.BANKID_ERROR_CANCELLED)
                    .put(BankIdError.TIMEOUT, LoginResultReason.BANKID_ERROR_TIMEOUT)
                    .put(BankIdError.NO_CLIENT, LoginResultReason.BANKID_ERROR_NO_CLIENT)
                    .put(
                            BankIdError.ALREADY_IN_PROGRESS,
                            LoginResultReason.BANKID_ERROR_ALREADY_IN_PROGRESS)
                    .put(BankIdError.INTERRUPTED, LoginResultReason.BANKID_ERROR_INTERRUPTED)
                    .put(
                            BankIdError.USER_VALIDATION_ERROR,
                            LoginResultReason.BANKID_ERROR_USER_VALIDATION_ERROR)
                    .put(
                            BankIdError.AUTHORIZATION_REQUIRED,
                            LoginResultReason.BANKID_ERROR_AUTHORIZATION_REQUIRED)
                    .put(
                            BankIdError.BANK_ID_UNAUTHORIZED_ISSUER,
                            LoginResultReason.BANKID_ERROR_BANK_ID_UNAUTHORIZED_ISSUER)
                    .put(BankIdError.BLOCKED, LoginResultReason.BANKID_ERROR_BLOCKED)
                    .put(
                            BankIdError.INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE,
                            LoginResultReason
                                    .BANKID_ERROR_INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE)
                    .build();

    private static final ImmutableMap<ThirdPartyAppError, LoginResultReason>
            THIRD_PARTY_APP_ERROR_MAPPER =
                    ImmutableMap.<ThirdPartyAppError, LoginResultReason>builder()
                            .put(
                                    ThirdPartyAppError.CANCELLED,
                                    LoginResultReason.THIRD_PARTY_APP_ERROR_CANCELLED)
                            .put(
                                    ThirdPartyAppError.TIMED_OUT,
                                    LoginResultReason.THIRD_PARTY_APP_ERROR_TIMED_OUT)
                            .put(
                                    ThirdPartyAppError.ALREADY_IN_PROGRESS,
                                    LoginResultReason.THIRD_PARTY_APP_ERROR_ALREADY_IN_PROGRESS)
                            .build();

    private static final ImmutableMap<SessionError, LoginResultReason> SESSION_ERROR_MAPPER =
            ImmutableMap.<SessionError, LoginResultReason>builder()
                    .put(
                            SessionError.SESSION_EXPIRED,
                            LoginResultReason.SESSION_ERROR_SESSION_EXPIRED)
                    .put(
                            SessionError.SESSION_ALREADY_ACTIVE,
                            LoginResultReason.SESSION_ERROR_SESSION_ALREADY_ACTIVE)
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
            LoginResultReason reason = LOGIN_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResultReason.LOGIN_ERROR_UNKNOWN);
        } else if (ex instanceof BankIdException) {
            BankIdError error = ((BankIdException) ex).getError();
            LoginResultReason reason = BANKID_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResultReason.BANKID_ERROR_UNKNOWN);
        } else if (ex instanceof ThirdPartyAppException) {
            ThirdPartyAppError error = ((ThirdPartyAppException) ex).getError();
            LoginResultReason reason = THIRD_PARTY_APP_ERROR_MAPPER.get(error);
            emitLoginResultEvent(
                    reason != null ? reason : LoginResultReason.THIRD_PARTY_APP_ERROR_UNKNOWN);
        } else if (ex instanceof SessionException) {
            SessionError error = ((SessionException) ex).getError();
            LoginResultReason reason = SESSION_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResultReason.SESSION_ERROR_UNKNOWN);
        } else {
            emitLoginResultEvent(LoginResultReason.UNKNOWN_ERROR);
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
