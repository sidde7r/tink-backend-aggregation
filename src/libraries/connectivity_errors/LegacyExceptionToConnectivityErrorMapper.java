package src.libraries.connectivity_errors;

import static java.util.Arrays.asList;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.SupplementalInfoError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdException;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.connectivity.errors.ConnectivityError;
import se.tink.connectivity.errors.ConnectivityErrorDetails;
import se.tink.connectivity.errors.ConnectivityErrorDetails.UserLoginErrors;

class LegacyExceptionToConnectivityErrorMapper {
    private static final Logger log = LoggerFactory.getLogger(ConnectivityErrorFactory.class);

    static final ImmutableMap<LoginError, ConnectivityError> LOGIN_ERROR_MAPPER =
            ImmutableMap.<LoginError, ConnectivityError>builder()
                    .put(
                            LoginError.NOT_CUSTOMER,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors.USER_NOT_A_CUSTOMER))
                    .put(
                            LoginError.NOT_SUPPORTED,
                            ConnectivityErrorFactory.tinkSideError(
                                    ConnectivityErrorDetails.TinkSideErrors
                                            .AUTHENTICATION_METHOD_NOT_SUPPORTED))
                    .put(
                            LoginError.INCORRECT_CREDENTIALS,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .STATIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.INCORRECT_CREDENTIALS_LAST_ATTEMPT,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .STATIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.INCORRECT_CHALLENGE_RESPONSE,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.CREDENTIALS_VERIFICATION_ERROR,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.WRONG_PHONENUMBER,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.ERROR_WITH_MOBILE_OPERATOR,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .THIRD_PARTY_AUTHENTICATION_UNAVAILABLE))
                    .put(
                            LoginError.REGISTER_DEVICE_ERROR, // change to generic user action
                            // required
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors
                                            .USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT))
                    .put(
                            LoginError.NO_ACCESS_TO_MOBILE_BANKING, // change to generic user action
                            // required
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors
                                            .USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT))
                    .put(
                            LoginError.INVALIDATED_CREDENTIALS,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .STATIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.WRONG_ACTIVATION_CODE,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.ACTIVATION_TIMED_OUT,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                    .put(
                            LoginError.NO_AVAILABLE_SCA_METHODS,
                            ConnectivityErrorFactory.tinkSideError(
                                    ConnectivityErrorDetails.TinkSideErrors
                                            .AUTHENTICATION_METHOD_NOT_SUPPORTED))

                    // we must either take a decision if we should introduce a UserLoginError of
                    // UNKNOWN type, or if all usages of LoginError.DEFAULT_MESSAGE should be fixed
                    // to be of a explicit type.
                    .put(
                            LoginError.DEFAULT_MESSAGE,
                            ConnectivityErrorFactory.userLoginError(
                                    UserLoginErrors.STATIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.PASSWORD_CHANGE_REQUIRED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .STATIC_CREDENTIALS_INCORRECT))
                    .put(
                            LoginError.NO_ACCOUNTS,
                            ConnectivityErrorFactory.accountInformationError(
                                    ConnectivityErrorDetails.AccountInformationErrors.NO_ACCOUNTS))
                    .build();

    static final ImmutableMap<BankIdError, ConnectivityError> BANKID_ERROR_MAPPER =
            ImmutableMap.<BankIdError, ConnectivityError>builder()
                    .put(
                            BankIdError.UNKNOWN,
                            ConnectivityErrorFactory.userLoginError(
                                    UserLoginErrors.THIRD_PARTY_AUTHENTICATION_UNAVAILABLE))
                    .put(
                            BankIdError.CANCELLED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_FLOW_CANCELLED))
                    .put(
                            BankIdError.TIMEOUT,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                    .put(
                            BankIdError.NO_CLIENT,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                    .put(
                            BankIdError.ALREADY_IN_PROGRESS,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .USER_CONCURRENT_LOGINS))
                    .put(
                            BankIdError.INTERRUPTED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .USER_CONCURRENT_LOGINS))
                    .put(
                            BankIdError.USER_VALIDATION_ERROR,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .STATIC_CREDENTIALS_INCORRECT))
                    .put(
                            BankIdError.AUTHORIZATION_REQUIRED,
                            ConnectivityErrorFactory.authorizationError( // generic user action
                                    ConnectivityErrorDetails.AuthorizationErrors
                                            .USER_ACTION_REQUIRED_UNSIGNED_AGREEMENT))
                    .put(
                            BankIdError.BANK_ID_UNAUTHORIZED_ISSUER,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors
                                            .ACTION_NOT_PERMITTED))
                    .put(
                            BankIdError.BLOCKED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED))
                    .put(
                            BankIdError.INVALID_STATUS_OF_MOBILE_BANKID_CERTIFICATE,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors
                                            .ACTION_NOT_PERMITTED))
                    .build();

    static final ImmutableMap<BankIdNOError, ConnectivityError> BANKID_NO_ERROR_MAPPER =
            ImmutableMap.<BankIdNOError, ConnectivityError>builder()
                    .put(
                            BankIdNOError.INITIALIZATION_ERROR,
                            ConnectivityErrorFactory.userLoginError(
                                    UserLoginErrors.THIRD_PARTY_AUTHENTICATION_UNAVAILABLE))
                    .put(
                            BankIdNOError.UNKNOWN_BANK_ID_ERROR,
                            ConnectivityErrorFactory.userLoginError(UserLoginErrors.UNRECOGNIZED))
                    .put(
                            BankIdNOError.INVALID_SSN_OR_ONE_TIME_CODE,
                            ConnectivityErrorFactory.userLoginError(
                                    UserLoginErrors.STATIC_CREDENTIALS_INCORRECT))
                    .put(
                            BankIdNOError.MOBILE_BANK_ID_TIMEOUT_OR_REJECTED,
                            ConnectivityErrorFactory.userLoginError(
                                    UserLoginErrors.DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                    .put(
                            BankIdNOError.BANK_ID_APP_BLOCKED,
                            ConnectivityErrorFactory.userLoginError(UserLoginErrors.USER_BLOCKED))
                    .put(
                            BankIdNOError.BANK_ID_APP_TIMEOUT,
                            ConnectivityErrorFactory.userLoginError(
                                    UserLoginErrors.DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                    .put(
                            BankIdNOError.INVALID_BANK_ID_PASSWORD,
                            ConnectivityErrorFactory.userLoginError(
                                    UserLoginErrors.STATIC_CREDENTIALS_INCORRECT))
                    .build();

    static final ImmutableMap<ThirdPartyAppError, ConnectivityError> THIRD_PARTY_APP_ERROR_MAPPER =
            ImmutableMap.<ThirdPartyAppError, ConnectivityError>builder()
                    .put(
                            ThirdPartyAppError.AUTHENTICATION_ERROR,
                            ConnectivityErrorFactory.userLoginError(
                                    UserLoginErrors.DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            ThirdPartyAppError.CANCELLED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_FLOW_CANCELLED))
                    .put(
                            ThirdPartyAppError.TIMED_OUT,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                    .put(
                            ThirdPartyAppError.ALREADY_IN_PROGRESS,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .USER_CONCURRENT_LOGINS))
                    .build();

    static final ImmutableMap<SessionError, ConnectivityError> SESSION_ERROR_MAPPER =
            ImmutableMap.<SessionError, ConnectivityError>builder()
                    .put(
                            SessionError.SESSION_EXPIRED,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED))
                    .put(
                            SessionError.SESSION_ALREADY_ACTIVE,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .USER_CONCURRENT_LOGINS))
                    .put(
                            SessionError.CONSENT_EXPIRED,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED))
                    .put(
                            SessionError.CONSENT_INVALID,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED))
                    .put(
                            SessionError.CONSENT_REVOKED,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED))
                    .put(
                            SessionError.CONSENT_REVOKED_BY_USER,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED))
                    .build();

    static final ImmutableMap<SupplementalInfoError, ConnectivityError>
            SUPPLEMENTAL_INFORMATION_ERROR_MAPPER =
                    ImmutableMap.<SupplementalInfoError, ConnectivityError>builder()
                            .put(
                                    SupplementalInfoError.NO_VALID_CODE,
                                    ConnectivityErrorFactory.userLoginError(
                                            ConnectivityErrorDetails.UserLoginErrors
                                                    .DYNAMIC_CREDENTIALS_INCORRECT))
                            .build();

    static final ImmutableMap<AuthorizationError, ConnectivityError> AUTHORIZATION_ERROR_MAPPER =
            ImmutableMap.<AuthorizationError, ConnectivityError>builder()
                    .put(
                            AuthorizationError.UNAUTHORIZED,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors
                                            .ACTION_NOT_PERMITTED))
                    .put(
                            AuthorizationError.NO_VALID_PROFILE,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors
                                            .ACTION_NOT_PERMITTED))
                    .put(
                            AuthorizationError.ACCOUNT_BLOCKED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED))
                    .build();

    static final ImmutableMap<BankServiceError, ConnectivityError> BANK_SERVICE_ERROR_MAPPER =
            ImmutableMap.<BankServiceError, ConnectivityError>builder()
                    .put(
                            BankServiceError.NO_BANK_SERVICE,
                            ConnectivityErrorFactory.providerError(
                                    ConnectivityErrorDetails.ProviderErrors.PROVIDER_UNAVAILABLE))
                    .put(
                            BankServiceError.BANK_SIDE_FAILURE,
                            ConnectivityErrorFactory.providerError(
                                    ConnectivityErrorDetails.ProviderErrors.PROVIDER_UNAVAILABLE))
                    .put(
                            BankServiceError.ACCESS_EXCEEDED,
                            ConnectivityErrorFactory.tinkSideError(
                                    ConnectivityErrorDetails.TinkSideErrors
                                            .TINK_INTERNAL_SERVER_ERROR))
                    .put(
                            BankServiceError.MULTIPLE_LOGIN,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .USER_CONCURRENT_LOGINS))
                    .put(
                            BankServiceError.SESSION_TERMINATED,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors.SESSION_EXPIRED))
                    .build();

    static final ImmutableMap<NemIdError, ConnectivityError> NEM_ID_ERROR_MAPPER =
            ImmutableMap.<NemIdError, ConnectivityError>builder()
                    .put(
                            NemIdError.CODE_TOKEN_NOT_SUPPORTED,
                            ConnectivityErrorFactory.tinkSideError(
                                    ConnectivityErrorDetails.TinkSideErrors
                                            .AUTHENTICATION_METHOD_NOT_SUPPORTED))
                    .put(
                            NemIdError.INTERRUPTED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .USER_CONCURRENT_LOGINS))
                    .put(
                            NemIdError.LOCKED_PIN,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED))
                    .put(
                            NemIdError.REJECTED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_FLOW_CANCELLED))
                    .put(
                            NemIdError.SECOND_FACTOR_NOT_REGISTERED,
                            ConnectivityErrorFactory.authorizationError(
                                    ConnectivityErrorDetails.AuthorizationErrors
                                            .ACTION_NOT_PERMITTED))
                    .put(
                            NemIdError.TIMEOUT,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_FLOW_TIMEOUT))
                    .put(
                            NemIdError.INVALID_CODE_CARD_CODE,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            NemIdError.USE_NEW_CODE_CARD,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            NemIdError.INVALID_CODE_TOKEN_CODE,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .DYNAMIC_CREDENTIALS_INCORRECT))
                    .put(
                            NemIdError.NEMID_LOCKED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED))
                    .put(
                            NemIdError.NEMID_BLOCKED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED))
                    .put(
                            NemIdError.NEMID_PASSWORD_BLOCKED,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED))
                    .put(
                            NemIdError.KEY_APP_NOT_READY_TO_USE,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors
                                            .THIRD_PARTY_AUTHENTICATION_UNAVAILABLE))
                    .put(
                            NemIdError.RENEW_NEMID,
                            ConnectivityErrorFactory.userLoginError(
                                    ConnectivityErrorDetails.UserLoginErrors.USER_BLOCKED))
                    .build();

    public static ConnectivityError from(Exception exception) {

        List<ConnectivityErrorCreator> connectivityErrorCreators =
                asList(
                        new AgentExceptionErrorCreator(),
                        new HttpClientExceptionErrorCreator(),
                        new JavaLangExceptionErrorCreator());

        for (ConnectivityErrorCreator errorCreator : connectivityErrorCreators) {
            Optional<ConnectivityError> maybeError =
                    errorCreator.tryCreateConnectivityErrorForException(exception);
            if (maybeError.isPresent()) {
                return maybeError.get();
            }
        }

        ConnectivityError unknownError =
                ConnectivityErrorFactory.tinkSideError(
                                ConnectivityErrorDetails.TinkSideErrors.UNKNOWN_ERROR)
                        .toBuilder()
                        .build();
        log.warn(
                "[Login Result debugging]: Exception ({}) did not map to any Connectivity Error",
                exception.getClass().getSimpleName(),
                exception);
        return unknownError;
    }

    private interface ConnectivityErrorCreator {
        Optional<ConnectivityError> tryCreateConnectivityErrorForException(Exception exception);
    }

    private static class AgentExceptionErrorCreator implements ConnectivityErrorCreator {

        public Optional<ConnectivityError> tryCreateConnectivityErrorForException(
                Exception exception) {
            ConnectivityError.Builder builder = null;

            /* Inherited from AgentException */
            if (exception instanceof BankIdException) {
                BankIdError error = ((BankIdException) exception).getError();
                builder = BANKID_ERROR_MAPPER.get(error).toBuilder();
            } else if (exception instanceof BankIdNOException) {
                BankIdNOError error = ((BankIdNOException) exception).getError();
                builder = BANKID_NO_ERROR_MAPPER.get(error).toBuilder();
            } else if (exception instanceof BankServiceException) {
                BankServiceError error = ((BankServiceException) exception).getError();
                builder = BANK_SERVICE_ERROR_MAPPER.get(error).toBuilder();
            } else if (exception instanceof LoginException) {
                LoginError error = ((LoginException) exception).getError();
                builder = LOGIN_ERROR_MAPPER.get(error).toBuilder();
            } else if (exception instanceof ThirdPartyAppException) {
                ThirdPartyAppError error = ((ThirdPartyAppException) exception).getError();
                builder = THIRD_PARTY_APP_ERROR_MAPPER.get(error).toBuilder();
            } else if (exception instanceof SessionException) {
                SessionError error = ((SessionException) exception).getError();
                builder = SESSION_ERROR_MAPPER.get(error).toBuilder();
            } else if (exception instanceof SupplementalInfoException) {
                SupplementalInfoError error = ((SupplementalInfoException) exception).getError();
                builder = SUPPLEMENTAL_INFORMATION_ERROR_MAPPER.get(error).toBuilder();
            } else if (exception instanceof NemIdException) {
                NemIdError error = ((NemIdException) exception).getError();
                builder = NEM_ID_ERROR_MAPPER.get(error).toBuilder();
            } else if (exception instanceof AuthorizationException) {
                AuthorizationError error = ((AuthorizationException) exception).getError();
                builder = AUTHORIZATION_ERROR_MAPPER.get(error).toBuilder();
            }

            return Optional.ofNullable(builder).map(ConnectivityError.Builder::build);
        }
    }

    private static class HttpClientExceptionErrorCreator implements ConnectivityErrorCreator {

        public Optional<ConnectivityError> tryCreateConnectivityErrorForException(
                Exception exception) {

            /* Http Client Exceptions */
            if (exception instanceof HttpClientException
                    || exception instanceof HttpResponseException
                    || exception instanceof ClientHandlerException
                    || exception instanceof UniformInterfaceException) {
                log.warn(
                        "Unhandled Http Client exceptions ({}) in agent. This should be fixed.",
                        exception.getClass().getSimpleName(),
                        exception);
                ConnectivityError.Builder builder =
                        ConnectivityErrorFactory.tinkSideError(
                                        ConnectivityErrorDetails.TinkSideErrors
                                                .TINK_INTERNAL_SERVER_ERROR)
                                .toBuilder();
                return Optional.of(builder.build());
            }

            return Optional.empty();
        }
    }

    private static class JavaLangExceptionErrorCreator implements ConnectivityErrorCreator {

        public Optional<ConnectivityError> tryCreateConnectivityErrorForException(
                Exception exception) {
            ConnectivityError.Builder builder = null;

            /* Java lang exceptions */
            if (exception instanceof NullPointerException
                    || exception instanceof IndexOutOfBoundsException
                    || exception instanceof NumberFormatException
                    || exception instanceof NotImplementedException) {
                log.error(
                        "Bug in agent code that throws {}. This should be fixed.",
                        exception.getClass().getSimpleName(),
                        exception);
                builder =
                        ConnectivityErrorFactory.tinkSideError(
                                        ConnectivityErrorDetails.TinkSideErrors
                                                .TINK_INTERNAL_SERVER_ERROR)
                                .toBuilder();
            } else if (exception instanceof IllegalStateException) {
                builder =
                        ConnectivityErrorFactory.tinkSideError(
                                        ConnectivityErrorDetails.TinkSideErrors
                                                .TINK_INTERNAL_SERVER_ERROR)
                                .toBuilder();
            }

            return Optional.ofNullable(builder).map(ConnectivityError.Builder::build);
        }
    }
}
