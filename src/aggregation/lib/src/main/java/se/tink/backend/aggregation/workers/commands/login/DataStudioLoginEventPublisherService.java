package se.tink.backend.aggregation.workers.commands.login;

import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOError;
import se.tink.backend.aggregation.agents.exceptions.bankidno.BankIdNOException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.connectivity.ConnectivityException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdException;
import se.tink.backend.aggregation.events.IntegrationParameters;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorType;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult;

@AllArgsConstructor
@Slf4j
public class DataStudioLoginEventPublisherService {

    private final LoginAgentEventProducer eventPublisher;
    private final long authenticationStartTime;
    private final AgentWorkerCommandContext context;

    void publishLoginSuccessEvent() {
        boolean wasAnyUserInteraction =
                MetricsFactory.wasAnyUserInteraction(
                        context.getRequest(), context.getSupplementalInteractionCounter());
        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult result =
                wasAnyUserInteraction
                        ? AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .SUCCESSFUL_LOGIN
                        : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .ALREADY_LOGGED_IN;
        publishLoginResultEvent(result);
    }

    void publishLoginBankIdErrorEvent(final BankIdException bankIdException) {
        BankIdError error = bankIdException.getError();
        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                BANKID_ERROR_MAPPER.get(error);
        publishLoginResultEvent(
                reason != null
                        ? reason
                        : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .BANKID_ERROR_UNKNOWN);
    }

    void publishLoginBankServiceErrorEvent(final BankServiceException bankServiceException) {
        BankServiceError error = bankServiceException.getError();
        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                BANK_SERVICE_ERROR_MAPPER.get(error);
        publishLoginResultEvent(
                reason != null
                        ? reason
                        : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .BANK_SERVICE_ERROR_UNKNOWN);
    }

    void publishLoginAuthenticationErrorEvent(
            final AuthenticationException authenticationException) {
        if (authenticationException instanceof LoginException) {
            LoginError error = ((LoginException) authenticationException).getError();
            AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                    LOGIN_ERROR_MAPPER.get(error);
            publishLoginResultEvent(
                    reason != null
                            ? reason
                            : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                    .LOGIN_ERROR_UNKNOWN);
        } else if (authenticationException instanceof ThirdPartyAppException) {
            ThirdPartyAppError error =
                    ((ThirdPartyAppException) authenticationException).getError();
            AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                    THIRD_PARTY_APP_ERROR_MAPPER.get(error);
            publishLoginResultEvent(
                    reason != null
                            ? reason
                            : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                    .THIRD_PARTY_APP_ERROR_UNKNOWN);
        } else if (authenticationException instanceof SessionException) {
            SessionError error = ((SessionException) authenticationException).getError();
            AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                    SESSION_ERROR_MAPPER.get(error);
            publishLoginResultEvent(
                    reason != null
                            ? reason
                            : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                    .SESSION_ERROR_UNKNOWN);
        } else if (authenticationException instanceof SupplementalInfoException) {
            SupplementalInfoError error =
                    ((SupplementalInfoException) authenticationException).getError();
            AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                    SUPPLEMENTAL_INFORMATION_ERROR_MAPPER.get(error);
            publishLoginResultEvent(
                    reason != null
                            ? reason
                            : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                    .SUPPLEMENTAL_INFO_ERROR_UNKNOWN);
        } else if (authenticationException instanceof NemIdException) {
            NemIdError error = ((NemIdException) authenticationException).getError();
            AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                    NEM_ID_ERROR_MAPPER.get(error);
            publishLoginResultEvent(
                    reason != null
                            ? reason
                            : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                    .NEMID_ERROR_UNKNOWN);
        } else if (authenticationException instanceof BankIdNOException) {
            BankIdNOError error = ((BankIdNOException) authenticationException).getError();
            AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                    BANKID_NO_ERROR_MAPPER.get(error);
            publishLoginResultEvent(
                    reason != null
                            ? reason
                            : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                    .BANKID_ERROR_UNKNOWN);
        } else {
            publishLoginResultEvent(
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                            .UNKNOWN_ERROR);
        }
    }

    void publishLoginAuthorizationErrorEvent(final AuthorizationException authorizationException) {
        AuthorizationError error = authorizationException.getError();
        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason =
                AUTHORIZATION_ERROR_MAPPER.get(error);
        publishLoginResultEvent(
                reason != null
                        ? reason
                        : AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult
                                .AUTHORIZATION_ERROR_UNKNOWN);
    }

    void publishLoginErrorUnknown() {
        publishLoginResultEvent(
                AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult.UNKNOWN_ERROR);
    }

    void publishEventForConnectivityException(final ConnectivityException connectivityException) {
        publishLoginResultEvent(
                CONNECTIVITY_ERROR_MAPPER.getOrDefault(
                        new ConnectivityErrorTypeAndReasonPair(connectivityException),
                        LoginResult.UNKNOWN_ERROR));
    }

    void publishLoginResultEvent(
            AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason) {
        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                userInteractionInformation =
                        AgentLoginCompletedEventUserInteractionInformationProvider
                                .userInteractionInformation(
                                        context.getSupplementalInteractionCounter(),
                                        context.getRequest());
        log.info(
                String.format(
                        "Authentication finished with %s and %s",
                        reason, userInteractionInformation));
        eventPublisher.sendLoginCompletedEvent(
                IntegrationParameters.builder()
                        .providerName(context.getRequest().getCredentials().getProviderName())
                        .correlationId(context.getCorrelationId())
                        .appId(context.getAppId())
                        .clusterId(context.getClusterId())
                        .userId(context.getRequest().getCredentials().getUserId())
                        .build(),
                reason,
                countAuthenticationElapsedTime(),
                userInteractionInformation);
    }

    private long countAuthenticationElapsedTime() {
        long finishTime = System.nanoTime();
        return finishTime - authenticationStartTime;
    }

    private static final ImmutableMap<
                    LoginError, AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            LOGIN_ERROR_MAPPER =
                    ImmutableMap
                            .<LoginError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    LoginError.NOT_CUSTOMER,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_NOT_CUSTOMER)
                            .put(
                                    LoginError.NOT_SUPPORTED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_NOT_SUPPORTED)
                            .put(
                                    LoginError.INCORRECT_CREDENTIALS,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult
                                            .LOGIN_ERROR_INCORRECT_CREDENTIALS_LAST_ATTEMPT)
                            .put(
                                    LoginError.INCORRECT_CHALLENGE_RESPONSE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CHALLENGE_RESPONSE)
                            .put(
                                    LoginError.CREDENTIALS_VERIFICATION_ERROR,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_CREDENTIALS_VERIFICATION_ERROR)
                            .put(
                                    LoginError.WRONG_PHONENUMBER,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_WRONG_PHONE_NUMBER)
                            .put(
                                    LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult
                                            .LOGIN_ERROR_WRONG_PHONE_NUMBER_OR_INACTIVATED_SERVICE)
                            .put(
                                    LoginError.ERROR_WITH_MOBILE_OPERATOR,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_ERROR_WITH_MOBILE_OPERATOR)
                            .put(
                                    LoginError.REGISTER_DEVICE_ERROR,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_REGISTER_DEVICE_ERROR)
                            .put(
                                    LoginError.NO_ACCESS_TO_MOBILE_BANKING,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_NO_ACCESS_TO_MOBILE_BANKING)
                            .put(
                                    LoginError.PASSWORD_CHANGE_REQUIRED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_PASSWORD_CHANGED)
                            .build();

    private static final ImmutableMap<
                    BankIdError, AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            BANKID_ERROR_MAPPER =
                    ImmutableMap
                            .<BankIdError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    BankIdError.CANCELLED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_CANCELLED)
                            .put(
                                    BankIdError.TIMEOUT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_TIMEOUT)
                            .put(
                                    BankIdError.NO_CLIENT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_NO_CLIENT)
                            .put(
                                    BankIdError.ALREADY_IN_PROGRESS,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_ALREADY_IN_PROGRESS)
                            .put(
                                    BankIdError.INTERRUPTED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_INTERRUPTED)
                            .put(
                                    BankIdError.USER_VALIDATION_ERROR,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_USER_VALIDATION_ERROR)
                            .put(
                                    BankIdError.AUTHORIZATION_REQUIRED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_AUTHORIZATION_REQUIRED)
                            .put(
                                    BankIdError.BANK_ID_UNAUTHORIZED_ISSUER,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_BANK_ID_UNAUTHORIZED_ISSUER)
                            .put(
                                    BankIdError.BLOCKED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_BLOCKED)
                            .put(
                                    BankIdError.INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult
                                            .BANKID_ERROR_INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE)
                            .build();

    private static final ImmutableMap<
                    BankIdNOError,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            BANKID_NO_ERROR_MAPPER =
                    ImmutableMap
                            .<BankIdNOError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    BankIdNOError.INVALID_SSN,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    BankIdNOError.INVALID_SSN_FORMAT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    BankIdNOError.INVALID_SSN_OR_ONE_TIME_CODE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    BankIdNOError.INVALID_ONE_TIME_CODE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    BankIdNOError.INVALID_ONE_TIME_CODE_FORMAT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    BankIdNOError.MOBILE_BANK_ID_TIMEOUT_OR_REJECTED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_TIMEOUT)
                            .put(
                                    BankIdNOError.THIRD_PARTY_APP_BLOCKED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_BLOCKED)
                            .put(
                                    BankIdNOError.THIRD_PARTY_APP_TIMEOUT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_TIMEOUT)
                            .put(
                                    BankIdNOError.THIRD_PARTY_APP_REJECTED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANKID_ERROR_CANCELLED)
                            .put(
                                    BankIdNOError.INVALID_BANK_ID_PASSWORD_FORMAT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    BankIdNOError.INVALID_BANK_ID_PASSWORD,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .build();

    private static final ImmutableMap<
                    ThirdPartyAppError,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            THIRD_PARTY_APP_ERROR_MAPPER =
                    ImmutableMap
                            .<ThirdPartyAppError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    ThirdPartyAppError.CANCELLED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_CANCELLED)
                            .put(
                                    ThirdPartyAppError.TIMED_OUT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_TIMED_OUT)
                            .put(
                                    ThirdPartyAppError.ALREADY_IN_PROGRESS,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_ALREADY_IN_PROGRESS)
                            .build();

    private static final ImmutableMap<
                    SessionError,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            SESSION_ERROR_MAPPER =
                    ImmutableMap
                            .<SessionError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    SessionError.SESSION_EXPIRED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_SESSION_EXPIRED)
                            .put(
                                    SessionError.SESSION_ALREADY_ACTIVE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_SESSION_ALREADY_ACTIVE)
                            .put(
                                    SessionError.CONSENT_EXPIRED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_CONSENT_EXPIRED)
                            .put(
                                    SessionError.CONSENT_INVALID,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_CONSENT_INVALID)
                            .put(
                                    SessionError.CONSENT_REVOKED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_CONSENT_REVOKED)
                            .put(
                                    SessionError.CONSENT_REVOKED_BY_USER,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_CONSENT_REVOKED_BY_USER)
                            .build();

    private static final ImmutableMap<
                    SupplementalInfoError,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            SUPPLEMENTAL_INFORMATION_ERROR_MAPPER =
                    ImmutableMap
                            .<SupplementalInfoError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            /**
                             * TODO: Provide a mapping for SUPPLEMENTAL_INFO_ERROR_UNKNOWN by
                             * extending the event.
                             */
                            .put(
                                    SupplementalInfoError.NO_VALID_CODE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SUPPLEMENTAL_INFO_ERROR_NO_VALID_CODE)
                            .put(
                                    SupplementalInfoError.ABORTED,
                                    LoginResult.SUPPLEMENTAL_INFO_CANCELLED)
                            .build();

    private static final ImmutableMap<
                    AuthorizationError,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            AUTHORIZATION_ERROR_MAPPER =
                    ImmutableMap
                            .<AuthorizationError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    AuthorizationError.UNAUTHORIZED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_UNAUTHORIZED)
                            .put(
                                    AuthorizationError.NO_VALID_PROFILE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_NO_VALID_PROFILE)
                            .put(
                                    AuthorizationError.ACCOUNT_BLOCKED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                            .build();

    private static final ImmutableMap<
                    BankServiceError,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            BANK_SERVICE_ERROR_MAPPER =
                    ImmutableMap
                            .<BankServiceError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    BankServiceError.NO_BANK_SERVICE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_NO_BANK_SERVICE)
                            .put(
                                    BankServiceError.BANK_SIDE_FAILURE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_BANK_SIDE_FAILURE)
                            .put(
                                    BankServiceError.ACCESS_EXCEEDED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_ACCESS_EXCEEDED)
                            .put(
                                    BankServiceError.MULTIPLE_LOGIN,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_MULTIPLE_LOGIN)
                            .put(
                                    BankServiceError.SESSION_TERMINATED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_SESSION_TERMINATED)
                            .put(
                                    BankServiceError.DEFAULT_MESSAGE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_BANK_SIDE_FAILURE)
                            .build();

    private static final ImmutableMap<
                    NemIdError, AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            NEM_ID_ERROR_MAPPER =
                    ImmutableMap
                            .<NemIdError,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    NemIdError.CODE_TOKEN_NOT_SUPPORTED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_NOT_SUPPORTED)
                            .put(
                                    NemIdError.INTERRUPTED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.NEMID_ERROR_INTERRUPTED)
                            .put(
                                    NemIdError.LOCKED_PIN,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                            .put(
                                    NemIdError.REJECTED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_CANCELLED)
                            .put(
                                    NemIdError.SECOND_FACTOR_NOT_REGISTERED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_NOT_SUPPORTED)
                            .put(
                                    NemIdError.INVALID_CODE_CARD_CODE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    NemIdError.USE_NEW_CODE_CARD,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    NemIdError.INVALID_CODE_TOKEN_CODE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    NemIdError.NEMID_LOCKED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                            .put(
                                    NemIdError.NEMID_BLOCKED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                            .put(
                                    NemIdError.NEMID_PASSWORD_BLOCKED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                            .put(
                                    NemIdError.KEY_APP_NOT_READY_TO_USE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_UNKNOWN)
                            .put(
                                    NemIdError.RENEW_NEMID,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                            .put(
                                    NemIdError.TIMEOUT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_TIMED_OUT)
                            .put(
                                    NemIdError.OLD_OTP_USED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.LOGIN_ERROR_INCORRECT_CHALLENGE_RESPONSE)
                            .build();

    private static final ImmutableMap<
                    ConnectivityErrorTypeAndReasonPair,
                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult>
            CONNECTIVITY_ERROR_MAPPER =
                    ImmutableMap
                            .<ConnectivityErrorTypeAndReasonPair,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult>
                                    builder()
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.TINK_SIDE_ERROR,
                                            ConnectivityErrorDetails.TinkSideErrors.UNKNOWN_ERROR
                                                    .name()),
                                    LoginResult.UNKNOWN_ERROR)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.TINK_SIDE_ERROR,
                                            ConnectivityErrorDetails.TinkSideErrors
                                                    .TINK_INTERNAL_SERVER_ERROR
                                                    .name()),
                                    LoginResult.UNKNOWN_ERROR)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.TINK_SIDE_ERROR,
                                            ConnectivityErrorDetails.TinkSideErrors
                                                    .OPERATION_NOT_SUPPORTED
                                                    .name()),
                                    LoginResult.LOGIN_ERROR_NOT_SUPPORTED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.TINK_SIDE_ERROR,
                                            ConnectivityErrorDetails.TinkSideErrors
                                                    .AUTHENTICATION_METHOD_NOT_SUPPORTED
                                                    .name()),
                                    LoginResult.LOGIN_ERROR_NOT_SUPPORTED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.TINK_SIDE_ERROR,
                                            ConnectivityErrorDetails.TinkSideErrors.TIMEOUT.name()),
                                    LoginResult.UNKNOWN_ERROR)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.TINK_SIDE_ERROR,
                                            ConnectivityErrorDetails.TinkSideErrors.UNRECOGNIZED
                                                    .name()),
                                    LoginResult.UNRECOGNIZED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.PROVIDER_ERROR,
                                            ConnectivityErrorDetails.ProviderErrors
                                                    .PROVIDER_UNAVAILABLE
                                                    .name()),
                                    LoginResult.BANK_SERVICE_ERROR_NO_BANK_SERVICE)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.PROVIDER_ERROR,
                                            ConnectivityErrorDetails.ProviderErrors
                                                    .LICENSED_PARTY_REJECTED
                                                    .name()),
                                    LoginResult.BANK_SERVICE_ERROR_UNKNOWN)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.PROVIDER_ERROR,
                                            ConnectivityErrorDetails.ProviderErrors.UNRECOGNIZED
                                                    .name()),
                                    LoginResult.UNRECOGNIZED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .THIRD_PARTY_AUTHENTICATION_UNAVAILABLE
                                                    .name()),
                                    LoginResult.THIRD_PARTY_APP_ERROR_UNKNOWN)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .STATIC_CREDENTIALS_INCORRECT
                                                    .name()),
                                    LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_INCORRECT
                                                    .name()),
                                    LoginResult.LOGIN_ERROR_INCORRECT_CREDENTIALS)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_FLOW_CANCELLED
                                                    .name()),
                                    LoginResult.SUPPLEMENTAL_INFO_CANCELLED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT
                                                    .name()),
                                    LoginResult.SUPPLEMENTAL_INFO_CANCELLED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .USER_NOT_A_CUSTOMER
                                                    .name()),
                                    LoginResult.LOGIN_ERROR_NOT_CUSTOMER)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .USER_CONCURRENT_LOGINS
                                                    .name()),
                                    LoginResult.BANK_SERVICE_ERROR_MULTIPLE_LOGIN)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED
                                                    .name()),
                                    LoginResult.AUTHORIZATION_ERROR_ACCOUNT_BLOCKED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.USER_LOGIN_ERROR,
                                            ConnectivityErrorDetails.UserLoginErrors.UNRECOGNIZED
                                                    .name()),
                                    LoginResult.UNRECOGNIZED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.AUTHORIZATION_ERROR,
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .ACTION_NOT_PERMITTED
                                                    .name()),
                                    LoginResult.BANKID_ERROR_BANK_ID_UNAUTHORIZED_ISSUER)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.AUTHORIZATION_ERROR,
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .SESSION_EXPIRED
                                                    .name()),
                                    LoginResult.SESSION_ERROR_SESSION_EXPIRED)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.AUTHORIZATION_ERROR,
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT
                                                    .name()),
                                    LoginResult.LOGIN_ERROR_NO_ACCESS_TO_MOBILE_BANKING)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.AUTHORIZATION_ERROR,
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .USER_ACTION_REQUIRED
                                                    .name()),
                                    LoginResult.AUTHORIZATION_ERROR_UNKNOWN)
                            .put(
                                    new ConnectivityErrorTypeAndReasonPair(
                                            ConnectivityErrorType.AUTHORIZATION_ERROR,
                                            ConnectivityErrorDetails.AuthorizationErrors
                                                    .UNRECOGNIZED
                                                    .name()),
                                    LoginResult.UNRECOGNIZED)
                            .build();
}
