package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

public class OAuth2Constants {
    public static class PersistentStorageKeys {
        public static final String ACCESS_TOKEN = "oauth2_access_token";
    }

    public static class CallbackParams {
        public static final String CODE = "code";
        public static final String ERROR = "error";
        public static final String ERROR_DESCRIPTION = "error_description";
    }

    public enum ErrorType {
        UNKNOWN("unknown"),
        ACCESS_DENIED("access_denied"),
        LOGIN_REQUIRED("login_required");

        private final String value;

        ErrorType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static ErrorType getErrorType(String value) {
            switch (value) {
                case "access_denied":
                    return ACCESS_DENIED;
                case "login_required":
                    return LOGIN_REQUIRED;
                default:
                    return UNKNOWN;
            }
        }
    }
}
