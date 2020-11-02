package se.tink.backend.aggregation.workers.commands.login;

import com.google.common.collect.ImmutableMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.agent.Agent;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.backend.aggregation.workers.metrics.MetricActionIface;
import se.tink.backend.aggregation.workers.operation.AgentWorkerCommandResult;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class LoginExecutor {

    private static final Logger log = LoggerFactory.getLogger(LoginExecutor.class);
    private List<LoginHandler> loginHandlerChain = new LinkedList<>();
    private List<LoginExceptionHandler> loginExceptionHandlerChain = new LinkedList<>();
    private final LoginAgentEventProducer loginAgentEventProducer;
    private long loginWorkerStartTimestamp;
    private AgentWorkerCommandContext context;
    private SupplementalInformationControllerUsageMonitorProxy
            supplementalInformationControllerUsageMonitorProxy;

    private static final ImmutableMap<LoginError, LoginResult> LOGIN_ERROR_MAPPER =
            ImmutableMap.<LoginError, LoginResult>builder()
                    .put(LoginError.NOT_CUSTOMER, LoginResult.LOGIN_ERROR_NOT_CUSTOMER)
                    .put(LoginError.NOT_SUPPORTED, LoginResult.LOGIN_ERROR_NOT_SUPPORTED)
                    .put(
                            LoginError.INCORRECT_CREDENTIALS,
                            LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                    .put(
                            LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT,
                            LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS_LAST_ATTEMPT)
                    .put(
                            LoginError.INCORRECT_CHALLENGE_RESPONSE,
                            LoginResult.LOGIN_ERROR_INCORRECT_CHALLENGE_RESPONSE)
                    .put(
                            LoginError.CREDENTIALS_VERIFICATION_ERROR,
                            LoginResult.LOGIN_ERROR_CREDENTIALS_VERIFICATION_ERROR)
                    .put(LoginError.WRONG_PHONENUMBER, LoginResult.LOGIN_ERROR_WRONG_PHONE_NUMBER)
                    .put(
                            LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE,
                            LoginResult.LOGIN_ERROR_WRONG_PHONE_NUMBER_OR_INACTIVATED_SERVICE)
                    .put(
                            LoginError.ERROR_WITH_MOBILE_OPERATOR,
                            LoginResult.LOGIN_ERROR_ERROR_WITH_MOBILE_OPERATOR)
                    .put(
                            LoginError.REGISTER_DEVICE_ERROR,
                            LoginResult.LOGIN_ERROR_REGISTER_DEVICE_ERROR)
                    .put(
                            LoginError.NO_ACCESS_TO_MOBILE_BANKING,
                            LoginResult.LOGIN_ERROR_NO_ACCESS_TO_MOBILE_BANKING)
                    .put(LoginError.PASSWORD_CHANGED, LoginResult.LOGIN_ERROR_PASSWORD_CHANGED)
                    .build();

    private static final ImmutableMap<BankIdError, LoginResult> BANKID_ERROR_MAPPER =
            ImmutableMap.<BankIdError, LoginResult>builder()
                    .put(BankIdError.CANCELLED, LoginResult.BANKID_ERROR_CANCELLED)
                    .put(BankIdError.TIMEOUT, LoginResult.BANKID_ERROR_TIMEOUT)
                    .put(BankIdError.NO_CLIENT, LoginResult.BANKID_ERROR_NO_CLIENT)
                    .put(
                            BankIdError.ALREADY_IN_PROGRESS,
                            LoginResult.BANKID_ERROR_ALREADY_IN_PROGRESS)
                    .put(BankIdError.INTERRUPTED, LoginResult.BANKID_ERROR_INTERRUPTED)
                    .put(
                            BankIdError.USER_VALIDATION_ERROR,
                            LoginResult.BANKID_ERROR_USER_VALIDATION_ERROR)
                    .put(
                            BankIdError.AUTHORIZATION_REQUIRED,
                            LoginResult.BANKID_ERROR_AUTHORIZATION_REQUIRED)
                    .put(
                            BankIdError.BANK_ID_UNAUTHORIZED_ISSUER,
                            LoginResult.BANKID_ERROR_BANK_ID_UNAUTHORIZED_ISSUER)
                    .put(BankIdError.BLOCKED, LoginResult.BANKID_ERROR_BLOCKED)
                    .put(
                            BankIdError.INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE,
                            LoginResult.BANKID_ERROR_INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE)
                    .build();

    private static final ImmutableMap<ThirdPartyAppError, LoginResult>
            THIRD_PARTY_APP_ERROR_MAPPER =
                    ImmutableMap.<ThirdPartyAppError, LoginResult>builder()
                            .put(
                                    ThirdPartyAppError.CANCELLED,
                                    LoginResult.THIRD_PARTY_APP_ERROR_CANCELLED)
                            .put(
                                    ThirdPartyAppError.TIMED_OUT,
                                    LoginResult.THIRD_PARTY_APP_ERROR_TIMED_OUT)
                            .put(
                                    ThirdPartyAppError.ALREADY_IN_PROGRESS,
                                    LoginResult.THIRD_PARTY_APP_ERROR_ALREADY_IN_PROGRESS)
                            .build();

    private static final ImmutableMap<SessionError, LoginResult> SESSION_ERROR_MAPPER =
            ImmutableMap.<SessionError, LoginResult>builder()
                    .put(SessionError.SESSION_EXPIRED, LoginResult.SESSION_ERROR_SESSION_EXPIRED)
                    .put(
                            SessionError.SESSION_ALREADY_ACTIVE,
                            LoginResult.SESSION_ERROR_SESSION_ALREADY_ACTIVE)
                    .put(
                            SessionError.CONSENT_EXPIRED,
                            LoginResult.BANK_SERVICE_ERROR_CONSENT_EXPIRED)
                    .put(
                            SessionError.CONSENT_INVALID,
                            LoginResult.BANK_SERVICE_ERROR_CONSENT_INVALID)
                    .put(
                            SessionError.CONSENT_REVOKED,
                            LoginResult.BANK_SERVICE_ERROR_CONSENT_REVOKED)
                    .put(
                            SessionError.CONSENT_REVOKED_BY_USER,
                            LoginResult.BANK_SERVICE_ERROR_CONSENT_REVOKED_BY_USER)
                    .build();

    private static final ImmutableMap<SupplementalInfoError, LoginResult>
            SUPPLEMENTAL_INFORMATION_ERROR_MAPPER =
                    ImmutableMap.<SupplementalInfoError, LoginResult>builder()
                            .put(
                                    SupplementalInfoError.NO_VALID_CODE,
                                    LoginResult.SUPPLEMENTAL_INFO_ERROR_NO_VALID_CODE)
                            .build();

    private static final ImmutableMap<AuthorizationError, LoginResult> AUTHORIZATION_ERROR_MAPPER =
            ImmutableMap.<AuthorizationError, LoginResult>builder()
                    .put(
                            AuthorizationError.UNAUTHORIZED,
                            LoginResult.AUTHORIZATION_ERROR_UNAUTHORIZED)
                    .put(
                            AuthorizationError.NO_VALID_PROFILE,
                            LoginResult.AUTHORIZATION_ERROR_NO_VALID_PROFILE)
                    .put(
                            AuthorizationError.ACCOUNT_BLOCKED,
                            LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                    .put(
                            AuthorizationError.DEVICE_LIMIT_REACHED,
                            LoginResult.AUTHORIZATION_ERROR_DEVICE_LIMIT_REACHED)
                    .put(
                            AuthorizationError.REACH_MAXIMUM_TRIES,
                            LoginResult.AUTHORIZATION_ERROR_REACH_MAXIMUM_TRIES)
                    .build();

    private static final ImmutableMap<BankServiceError, LoginResult> BANK_SERVICE_ERROR_MAPPER =
            ImmutableMap.<BankServiceError, LoginResult>builder()
                    .put(
                            BankServiceError.NO_BANK_SERVICE,
                            LoginResult.BANK_SERVICE_ERROR_NO_BANK_SERVICE)
                    .put(
                            BankServiceError.BANK_SIDE_FAILURE,
                            LoginResult.BANK_SERVICE_ERROR_BANK_SIDE_FAILURE)
                    .put(
                            BankServiceError.ACCESS_EXCEEDED,
                            LoginResult.BANK_SERVICE_ERROR_ACCESS_EXCEEDED)
                    .put(
                            BankServiceError.CONSENT_EXPIRED,
                            LoginResult.BANK_SERVICE_ERROR_CONSENT_EXPIRED)
                    .put(
                            BankServiceError.CONSENT_INVALID,
                            LoginResult.BANK_SERVICE_ERROR_CONSENT_INVALID)
                    .put(
                            BankServiceError.CONSENT_REVOKED_BY_USER,
                            LoginResult.BANK_SERVICE_ERROR_CONSENT_REVOKED_BY_USER)
                    .put(
                            BankServiceError.CONSENT_REVOKED,
                            LoginResult.BANK_SERVICE_ERROR_CONSENT_REVOKED)
                    .put(
                            BankServiceError.MULTIPLE_LOGIN,
                            LoginResult.BANK_SERVICE_ERROR_MULTIPLE_LOGIN)
                    .put(
                            BankServiceError.SESSION_TERMINATED,
                            LoginResult.BANK_SERVICE_ERROR_SESSION_TERMINATED)
                    .build();

    private static final ImmutableMap<NemIdError, LoginResult> NEM_ID_ERROR_MAPPER =
            ImmutableMap.<NemIdError, LoginResult>builder()
                    .put(NemIdError.CODEAPP_NOT_REGISTERED, LoginResult.LOGIN_ERROR_NOT_SUPPORTED)
                    .put(NemIdError.INTERRUPTED, LoginResult.NEMID_ERROR_INTERRUPTED)
                    .put(NemIdError.LOCKED_PIN, LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                    .put(
                            NemIdError.REJECTED,
                            LoginResult.LOGIN_ERROR_CREDENTIALS_VERIFICATION_ERROR)
                    .put(
                            NemIdError.SECOND_FACTOR_NOT_REGISTERED,
                            LoginResult.LOGIN_ERROR_NOT_SUPPORTED)
                    .put(NemIdError.TIMEOUT, LoginResult.THIRD_PARTY_APP_ERROR_TIMED_OUT)
                    .build();

    public LoginExecutor(
            final StatusUpdater statusUpdater,
            final AgentWorkerCommandContext context,
            final SupplementalInformationControllerUsageMonitorProxy
                    supplementalInformationControllerUsageMonitorProxy,
            final LoginAgentEventProducer loginAgentEventProducer,
            final long loginWorkerStartTimestamp) {
        this.supplementalInformationControllerUsageMonitorProxy =
                supplementalInformationControllerUsageMonitorProxy;
        this.loginAgentEventProducer = loginAgentEventProducer;
        this.loginWorkerStartTimestamp = loginWorkerStartTimestamp;
        this.context = context;
        initLoginHandlerChain();
        initLoginExceptionHandlerChain(statusUpdater);
    }

    private void emitLoginResultEvent(LoginResult reason) {

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

    private void initLoginHandlerChain() {
        loginHandlerChain.add(
                new ProgressiveAuthenticatorLoginHandler(
                        supplementalInformationControllerUsageMonitorProxy));
        loginHandlerChain.add(new DefaultAgentLoginController());
        loginHandlerChain.add(new LoginFailedHandler());
    }

    private void initLoginExceptionHandlerChain(final StatusUpdater statusUpdater) {
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
            final CredentialsRequest credentialsRequest) {
        try {
            log.info(
                    "LoginExecutor for credentials {}",
                    Optional.ofNullable(credentialsRequest.getCredentials())
                            .map(Credentials::getId)
                            .orElse(null));
            return processLoginHandlerChain(agent, metricAction, credentialsRequest);
        } catch (Exception ex) {
            log.info(
                    "LoginExecutor-EXCEPTION for credentials {}",
                    Optional.ofNullable(credentialsRequest.getCredentials())
                            .map(Credentials::getId)
                            .orElse(null),
                    ex);
            return processLoginExceptionHandlerChain(ex, metricAction);
        }
    }

    private AgentWorkerCommandResult processLoginHandlerChain(
            final Agent agent,
            final MetricActionIface metricAction,
            final CredentialsRequest credentialsRequest)
            throws Exception {
        for (LoginHandler loginHandler : loginHandlerChain) {
            Optional<AgentWorkerCommandResult> result =
                    loginHandler.handleLogin(agent, metricAction, credentialsRequest);
            if (result.isPresent()) {
                switch (result.get()) {
                    case CONTINUE:
                        publishSuccessLoginResultEvent();
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

    private void publishSuccessLoginResultEvent() {
        LoginResult result =
                supplementalInformationControllerUsageMonitorProxy.isUsed()
                        ? LoginResult.SUCCESSFUL_LOGIN
                        : LoginResult.ALREADY_LOGGED_IN;
        emitLoginResultEvent(result);
    }

    private AgentWorkerCommandResult processLoginExceptionHandlerChain(
            final Exception ex, final MetricActionIface metricAction) {

        // We emit proper login event that contains the proper reason for the error here
        if (ex instanceof LoginException) {
            LoginError error = ((LoginException) ex).getError();
            LoginResult reason = LOGIN_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResult.LOGIN_ERROR_UNKNOWN);
        } else if (ex instanceof BankIdException) {
            BankIdError error = ((BankIdException) ex).getError();
            LoginResult reason = BANKID_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResult.BANKID_ERROR_UNKNOWN);
        } else if (ex instanceof ThirdPartyAppException) {
            ThirdPartyAppError error = ((ThirdPartyAppException) ex).getError();
            LoginResult reason = THIRD_PARTY_APP_ERROR_MAPPER.get(error);
            emitLoginResultEvent(
                    reason != null ? reason : LoginResult.THIRD_PARTY_APP_ERROR_UNKNOWN);
        } else if (ex instanceof SessionException) {
            SessionError error = ((SessionException) ex).getError();
            LoginResult reason = SESSION_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResult.SESSION_ERROR_UNKNOWN);
        } else if (ex instanceof SupplementalInfoException) {
            SupplementalInfoError error = ((SupplementalInfoException) ex).getError();
            LoginResult reason = SUPPLEMENTAL_INFORMATION_ERROR_MAPPER.get(error);
            emitLoginResultEvent(
                    reason != null ? reason : LoginResult.SUPPLEMENTAL_INFO_ERROR_UNKNOWN);
        } else if (ex instanceof AuthorizationException) {
            AuthorizationError error = ((AuthorizationException) ex).getError();
            LoginResult reason = AUTHORIZATION_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResult.AUTHORIZATION_ERROR_UNKNOWN);
        } else if (ex instanceof BankServiceException) {
            BankServiceError error = ((BankServiceException) ex).getError();
            LoginResult reason = BANK_SERVICE_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResult.BANK_SERVICE_ERROR_UNKNOWN);
        } else if (ex instanceof NemIdException) {
            NemIdError error = ((NemIdException) ex).getError();
            LoginResult reason = NEM_ID_ERROR_MAPPER.get(error);
            emitLoginResultEvent(reason != null ? reason : LoginResult.NEMID_ERROR_UNKNOWN);
        } else {
            emitLoginResultEvent(LoginResult.UNKNOWN_ERROR);
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
