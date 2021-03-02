package src.libraries.connectivity_errors;

import com.google.common.collect.ImmutableMap;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.connectivity.errors.ConnectivityErrorType;

class LegacyExceptionToConnectivityErrorMapper {
    static final ImmutableMap<LoginError, ConnectivityErrorType> LOGIN_ERROR_MAPPER =
            ImmutableMap.<LoginError, ConnectivityErrorType>builder()
                    .put(LoginError.NOT_CUSTOMER, ConnectivityErrorType.ERROR_USER_NOT_A_CUSTOMER)
                    .put( // This should probably get it's own type
                            LoginError.NOT_SUPPORTED,
                            ConnectivityErrorType.ERROR_TINK_INTERNAL_ERROR)
                    .put(
                            LoginError.INCORRECT_CREDENTIALS,
                            ConnectivityErrorType.ERROR_AUTH_STATIC_CREDENTIALS_INCORRECT)
                    .put(
                            LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT,
                            ConnectivityErrorType.ERROR_AUTH_STATIC_CREDENTIALS_INCORRECT)
                    .put(
                            LoginError.INCORRECT_CHALLENGE_RESPONSE,
                            ConnectivityErrorType.ERROR_AUTH_DYNAMIC_CREDENTIALS_INCORRECT)
                    .put(
                            LoginError.CREDENTIALS_VERIFICATION_ERROR,
                            ConnectivityErrorType.ERROR_AUTH_DYNAMIC_CREDENTIALS_INCORRECT)
                    .put(
                            LoginError.WRONG_PHONENUMBER,
                            ConnectivityErrorType.ERROR_AUTH_DYNAMIC_CREDENTIALS_INCORRECT)
                    .put(
                            LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE,
                            ConnectivityErrorType.ERROR_AUTH_DYNAMIC_CREDENTIALS_INCORRECT)
                    .put(
                            LoginError.ERROR_WITH_MOBILE_OPERATOR,
                            ConnectivityErrorType.ERROR_AUTH_THIRD_PARTY_UNAVAILABLE)
                    .put(
                            LoginError.REGISTER_DEVICE_ERROR,
                            ConnectivityErrorType.ERROR_USER_ACTION_REQUIRED)
                    .put(
                            LoginError.NO_ACCESS_TO_MOBILE_BANKING,
                            ConnectivityErrorType.ERROR_USER_ACTION_REQUIRED)
                    .build();

    static final ImmutableMap<BankIdError, ConnectivityErrorType> BANKID_ERROR_MAPPER =
            ImmutableMap.<BankIdError, ConnectivityErrorType>builder()
                    .put(
                            BankIdError.CANCELLED,
                            ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_CANCELLED)
                    .put(BankIdError.TIMEOUT, ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_TIMEOUT)
                    .put(
                            BankIdError.NO_CLIENT,
                            ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_TIMEOUT)
                    .put(
                            BankIdError.ALREADY_IN_PROGRESS,
                            ConnectivityErrorType.ERROR_USER_MULTIPLE_LOGINS)
                    .put(BankIdError.INTERRUPTED, ConnectivityErrorType.ERROR_USER_MULTIPLE_LOGINS)
                    .put(
                            BankIdError.USER_VALIDATION_ERROR,
                            ConnectivityErrorType.ERROR_AUTH_STATIC_CREDENTIALS_INCORRECT)
                    .put(
                            BankIdError.AUTHORIZATION_REQUIRED,
                            ConnectivityErrorType.ERROR_USER_ACTION_REQUIRED)
                    .put(
                            BankIdError.BANK_ID_UNAUTHORIZED_ISSUER,
                            ConnectivityErrorType.ERROR_USER_ACTION_NOT_PERMITTED)
                    .put(BankIdError.BLOCKED, ConnectivityErrorType.ERROR_USER_ACTION_NOT_PERMITTED)
                    .put(
                            BankIdError.INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE,
                            ConnectivityErrorType.ERROR_USER_ACTION_NOT_PERMITTED)
                    .build();

    static final ImmutableMap<ThirdPartyAppError, ConnectivityErrorType>
            THIRD_PARTY_APP_ERROR_MAPPER =
                    ImmutableMap.<ThirdPartyAppError, ConnectivityErrorType>builder()
                            .put(
                                    ThirdPartyAppError.CANCELLED,
                                    ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_CANCELLED)
                            .put(
                                    ThirdPartyAppError.TIMED_OUT,
                                    ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_TIMEOUT)
                            .put(
                                    ThirdPartyAppError.ALREADY_IN_PROGRESS,
                                    ConnectivityErrorType.ERROR_USER_MULTIPLE_LOGINS)
                            .build();

    static final ImmutableMap<SessionError, ConnectivityErrorType> SESSION_ERROR_MAPPER =
            ImmutableMap.<SessionError, ConnectivityErrorType>builder()
                    .put(
                            SessionError.SESSION_EXPIRED,
                            ConnectivityErrorType.ERROR_USER_SESSION_EXPIRED)
                    .put(
                            SessionError.SESSION_ALREADY_ACTIVE,
                            ConnectivityErrorType.ERROR_USER_MULTIPLE_LOGINS)
                    .put(
                            SessionError.CONSENT_EXPIRED,
                            ConnectivityErrorType.ERROR_USER_SESSION_EXPIRED)
                    .put(
                            SessionError.CONSENT_INVALID,
                            ConnectivityErrorType.ERROR_USER_SESSION_EXPIRED)
                    .put(
                            SessionError.CONSENT_REVOKED,
                            ConnectivityErrorType.ERROR_USER_SESSION_EXPIRED)
                    .put(
                            SessionError.CONSENT_REVOKED_BY_USER,
                            ConnectivityErrorType.ERROR_USER_SESSION_EXPIRED)
                    .build();

    static final ImmutableMap<SupplementalInfoError, ConnectivityErrorType>
            SUPPLEMENTAL_INFORMATION_ERROR_MAPPER =
                    ImmutableMap.<SupplementalInfoError, ConnectivityErrorType>builder()
                            .put(
                                    SupplementalInfoError.NO_VALID_CODE,
                                    ConnectivityErrorType.ERROR_AUTH_DYNAMIC_CREDENTIALS_INCORRECT)
                            .build();

    static final ImmutableMap<AuthorizationError, ConnectivityErrorType>
            AUTHORIZATION_ERROR_MAPPER =
                    ImmutableMap.<AuthorizationError, ConnectivityErrorType>builder()
                            .put(
                                    AuthorizationError.UNAUTHORIZED,
                                    ConnectivityErrorType.ERROR_USER_ACTION_NOT_PERMITTED)
                            .put(
                                    AuthorizationError.NO_VALID_PROFILE,
                                    ConnectivityErrorType.ERROR_USER_ACTION_NOT_PERMITTED)
                            .put(
                                    AuthorizationError.ACCOUNT_BLOCKED,
                                    ConnectivityErrorType.ERROR_USER_ACTION_NOT_PERMITTED)
                            .build();

    static final ImmutableMap<BankServiceError, ConnectivityErrorType> BANK_SERVICE_ERROR_MAPPER =
            ImmutableMap.<BankServiceError, ConnectivityErrorType>builder()
                    .put(
                            BankServiceError.NO_BANK_SERVICE,
                            ConnectivityErrorType.ERROR_PROVIDER_UNAVAILABLE)
                    .put(
                            BankServiceError.BANK_SIDE_FAILURE,
                            ConnectivityErrorType.ERROR_AUTH_THIRD_PARTY_UNAVAILABLE)
                    .put(
                            BankServiceError.ACCESS_EXCEEDED,
                            ConnectivityErrorType.ERROR_TINK_INTERNAL_ERROR)
                    .put(
                            BankServiceError.MULTIPLE_LOGIN,
                            ConnectivityErrorType.ERROR_USER_MULTIPLE_LOGINS)
                    .put(
                            BankServiceError.SESSION_TERMINATED,
                            ConnectivityErrorType.ERROR_USER_SESSION_EXPIRED)
                    .build();

    static final ImmutableMap<NemIdError, ConnectivityErrorType> NEM_ID_ERROR_MAPPER =
            ImmutableMap.<NemIdError, ConnectivityErrorType>builder()
                    // This should probably get it's own type (same as LoginError.NOT_SUPPORTED
                    // would be mapped to
                    .put(
                            NemIdError.CODE_TOKEN_NOT_SUPPORTED,
                            ConnectivityErrorType.ERROR_TINK_INTERNAL_ERROR)
                    .put(NemIdError.INTERRUPTED, ConnectivityErrorType.ERROR_USER_MULTIPLE_LOGINS)
                    .put(
                            NemIdError.LOCKED_PIN,
                            ConnectivityErrorType.ERROR_USER_ACTION_NOT_PERMITTED)
                    .put(
                            NemIdError.REJECTED,
                            ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_CANCELLED)
                    .put(
                            NemIdError.SECOND_FACTOR_NOT_REGISTERED,
                            ConnectivityErrorType.ERROR_USER_ACTION_NOT_PERMITTED)
                    .put(NemIdError.TIMEOUT, ConnectivityErrorType.ERROR_AUTH_DYNAMIC_FLOW_TIMEOUT)
                    .build();
}
