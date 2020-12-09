package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import java.util.Optional;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.ErrorEntity;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.libraries.i18n.LocalizableKey;

public class SamlinkConstants {

    public static final class Storage {
        public static final String ACCESS_TOKEN = "access_token";
        public static final String DEVICE_ID = "device_id";
        public static final String DEVICE_TOKEN = "device_token";
        public static final String SERVICES_ENDPOINTS = "services_endpoints";
        public static final String LOGIN_NAME = "login_name";
    }

    public static final class Url {
        static final String BASE_PATH = "/api/";
    }

    public static class QueryParams {
        public static final String QUERY_PARAM_LIMIT = "limit";
        public static final String QUERY_PARAM_LIMIT_ACCOUNT_DEFAULT = "999";
        public static final String QUERY_PARAM_LIMIT_TX_DEFAULT = "30";
        public static final String QUERY_PARAM_OFFSET = "offset";
        public static final String QUERY_PARAM_OFFSET_DEFAULT = "0";
    }

    public static final class LinkRel {
        public static final String IDENTIFICATION = "identification";
        public static final String AUTHENTICATION = "authentication";
        public static final String SERVICES = "services";
        public static final String ACCOUNTS = "accounts";
        public static final String TRANSACTIONS = "transactions";
        public static final String NEXT = "next";
        public static final String DETAILS = "details";
        public static final String CARDS = "cards";
        public static final String LOANS = "loans";
        public static final String COUNTS = "counts";
    }

    public static final class ErrorMessage {
        private static final LocalizableKey INVALID_USERNAME_PASSWORD =
                new LocalizableKey("Invalid username or password");
        private static final LocalizableKey CONTRACT_MISSING =
                new LocalizableKey("Contract is missing.");
        private static final LocalizableKey CONTRACT_TRANSFERRED =
                new LocalizableKey("Contract has been transferred " + "to other bank.");
        private static final LocalizableKey USER_LOCKED =
                new LocalizableKey("User account is locked. Please contact " + "your bank.");
        private static final LocalizableKey USER_BLOCKED =
                new LocalizableKey("User account is blocked. Please " + "contact your bank.");
        public static final LocalizableKey DEVICE_PINNING_FAILED =
                new LocalizableKey("Too many devices registered. " + "Please contact your bank.");
    }

    public enum ServerError {
        WRONG_ORGANIZATION(
                "SecurityException.WRONG_ORGANIZATION",
                ErrorMessage.INVALID_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS),
        BANK_LOGIN_FORBIDDEN(
                "SecurityException.BANK_LOGIN_FORBIDDEN",
                ErrorMessage.INVALID_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS),
        LOGIN_FAILED(
                "LoginServiceException.LOGIN_FAILED",
                ErrorMessage.INVALID_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS),
        PASSWORD_IS_INVALID(
                "LoginServiceException.PASSWORD_IS_INVALID",
                ErrorMessage.INVALID_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS),
        ACCOUNT_NOT_FOUND(
                "AccountServiceException.ACCOUNT_NOT_FOUND",
                ErrorMessage.INVALID_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS),
        INVALID_PASSWORD(
                "AccessManagementServiceException.INVALID_PASSWORD",
                ErrorMessage.INVALID_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS),
        LOGIN_CONTRACT_IS_MISSING(
                "LoginServiceException.CONTRACT_IS_MISSING",
                ErrorMessage.CONTRACT_MISSING,
                LoginError.INCORRECT_CREDENTIALS),
        LOGIN_CONTRACT_TRANSFERRED(
                "LoginServiceException.CONTRACTS_BEEN_TRANSFERRED",
                ErrorMessage.CONTRACT_TRANSFERRED,
                LoginError.INCORRECT_CREDENTIALS),
        INVALID_KEY(
                "SecurityKeyServiceException.INVALID_KEY",
                ErrorMessage.INVALID_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS),
        WRONG_SECURITY_KEY(
                "LoginServiceException.WRONG_SECURITY_KEY",
                ErrorMessage.INVALID_USERNAME_PASSWORD,
                LoginError.INCORRECT_CREDENTIALS),
        INVALID_CARD_CODE(
                "SecurityKeyServiceException.WRONG_SECURITY_KEY",
                LoginError.INCORRECT_CHALLENGE_RESPONSE),

        ACCESS_CONTRACT_IS_MISSING(
                "AccessManagementServiceException.CONTRACT_IS_MISSING",
                ErrorMessage.CONTRACT_MISSING,
                LoginError.INCORRECT_CREDENTIALS),
        SECURITY_CONTRACT_IS_MISSING(
                "SecurityKeyServiceException.CONTRACT_IS_MISSING",
                ErrorMessage.CONTRACT_MISSING,
                LoginError.INCORRECT_CREDENTIALS),

        USER_ID_IS_BLOCKED(
                "LoginServiceException.USER_ID_IS_BLOCKED",
                ErrorMessage.USER_BLOCKED,
                LoginError.INCORRECT_CREDENTIALS),

        ACCESS_USER_ID_IS_LOCKED(
                "AccessManagementServiceException.USER_ID_IS_LOCKED",
                ErrorMessage.USER_LOCKED,
                LoginError.INCORRECT_CREDENTIALS),
        SECURITY_USER_ID_IS_LOCKED(
                "SecurityKeyServiceException.USER_ID_IS_LOCKED",
                ErrorMessage.USER_LOCKED,
                LoginError.INCORRECT_CREDENTIALS),
        LOGIN_USER_LOCKED(
                "LoginServiceException.USER_ID_IS_LOCKED",
                ErrorMessage.USER_LOCKED,
                LoginError.INCORRECT_CREDENTIALS),
        MAX_DEVICE_LIMIT_REACHED(
                "SettingsServiceException.MAX_DEVICE_LIMIT_REACHED",
                ErrorMessage.DEVICE_PINNING_FAILED,
                AuthorizationError.ACCOUNT_BLOCKED),
        VALIDATION_ERROR(
                "CodedError.VALIDATION_ERROR",
                ErrorMessage.CONTRACT_MISSING,
                LoginError.INCORRECT_CREDENTIALS);

        private final String errorCode;
        private final LocalizableKey errorMessage;
        private final AgentError agentError;

        ServerError(String errorCode, LocalizableKey errorMessage, AgentError agentError) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
            this.agentError = agentError;
        }

        ServerError(String errorCode, AgentError agentError) {
            this(errorCode, null, agentError);
        }

        public static Optional<ServerError> findServerError(ErrorEntity error) {
            return Stream.of(values())
                    .filter(serverError -> serverError.errorCode.equalsIgnoreCase(error.getCode()))
                    .findFirst();
        }

        public boolean hasCode(String code) {
            return this.errorCode.equalsIgnoreCase(code);
        }

        public AgentException exception() {
            return Optional.ofNullable(this.errorMessage)
                    .map(this.agentError::exception)
                    .orElseGet(this.agentError::exception);
        }
    }

    public static final class LoanType {
        public static final String MORTGAGE = "HOUSING";
        public static final String STUDENT = "STUDENT";
        public static final String OTHER = "OTHER";
    }

    public static final class LogTags {
        public static final LogTag AUTHENTICATION = LogTag.from("#samlink_authentication");
        public static final LogTag CREDITCARD = LogTag.from("#samlink_creditcard");
        public static final LogTag UNKNOWN_LOAN_TYPE = LogTag.from("#samlink_unknown_loan_type");
    }

    public static class Header {
        public static final String CLIENT_VERSION = "Client-Version";
        public static final String VALUE_CLIENT_VERSION_V1 = "1.3.9";
        public static final String VALUE_CLIENT_VERSION_V2 = "2.3.2";

        public static final String VALUE_ACCEPT_V1 = "application/vnd.vepa.mobile.api-v1+json";
        public static final String VALUE_ACCEPT_V2 = "application/vnd.vepa.mobile.api-v2+json";

        public static final String CLIENT_APP = "Client-App";

        public static final String API_KEY = "X-API-Key";
        public static final String API_KEY_VALUE = "a4302c76-6bc7-40de-be73-2be3c62efb93";
    }
}
