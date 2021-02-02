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
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.events.IntegrationParameters;
import se.tink.backend.aggregation.events.LoginAgentEventProducer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdException;
import se.tink.backend.aggregation.workers.context.AgentWorkerCommandContext;
import se.tink.eventproducerservice.events.grpc.AgentLoginCompletedEventProto;

@AllArgsConstructor
@Slf4j
public class DataStudioLoginEventPublisherService {

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
                                    LoginError.PASSWORD_CHANGED,
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
                            .put(
                                    SupplementalInfoError.NO_VALID_CODE,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SUPPLEMENTAL_INFO_ERROR_NO_VALID_CODE)
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
                            .put(
                                    AuthorizationError.DEVICE_LIMIT_REACHED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_DEVICE_LIMIT_REACHED)
                            .put(
                                    AuthorizationError.REACH_MAXIMUM_TRIES,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.AUTHORIZATION_ERROR_REACH_MAXIMUM_TRIES)
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
                                    BankServiceError.CONSENT_EXPIRED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_CONSENT_EXPIRED)
                            .put(
                                    BankServiceError.CONSENT_INVALID,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_CONSENT_INVALID)
                            .put(
                                    BankServiceError.CONSENT_REVOKED_BY_USER,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_CONSENT_REVOKED_BY_USER)
                            .put(
                                    BankServiceError.CONSENT_REVOKED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.SESSION_ERROR_CONSENT_REVOKED)
                            .put(
                                    BankServiceError.MULTIPLE_LOGIN,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_MULTIPLE_LOGIN)
                            .put(
                                    BankServiceError.SESSION_TERMINATED,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.BANK_SERVICE_ERROR_SESSION_TERMINATED)
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
                                    NemIdError.CODEAPP_NOT_REGISTERED,
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
                                    NemIdError.TIMEOUT,
                                    AgentLoginCompletedEventProto.AgentLoginCompletedEvent
                                            .LoginResult.THIRD_PARTY_APP_ERROR_TIMED_OUT)
                            .build();

    private final LoginAgentEventProducer eventPublisher;
    private final long authenticationStartTime;
    private final AgentWorkerCommandContext context;

    void publishLoginSuccessEvent() {
        int interactions = context.getSupplementalInteractionCounter().getNumberInteractions();
        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult result =
                interactions == 0
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

    void publishLoginResultEvent(
            AgentLoginCompletedEventProto.AgentLoginCompletedEvent.LoginResult reason) {
        AgentLoginCompletedEventProto.AgentLoginCompletedEvent.UserInteractionInformation
                userInteractionInformation =
                        AgentLoginCompletedEventUserInteractionInformationProvider
                                .userInteractionInformation(
                                        context.getSupplementalInteractionCounter());
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
}
