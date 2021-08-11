package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants;

import java.util.stream.Stream;

public class OAuth2Constants {
    public static class PersistentStorageKeys {
        public static final String OAUTH_2_TOKEN = "oauth2_access_token";
    }

    public static class CallbackParams {
        public static final String CODE = "code";
        public static final String ERROR = "error";
        public static final String ERROR_DESCRIPTION = "error_description";
    }

    public enum ErrorType {
        UNKNOWN("unknown"),
        ACCESS_DENIED("access_denied"),
        INVALID_REQUEST("invalid_request"),
        UNAUTHORIZED_CLIENT("unauthorized_client"),
        UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type"),
        INVALID_SCOPE("invalid_scope"),
        SERVER_ERROR("server_error"),
        TEMPORARILY_UNAVAILABLE("temporarily_unavailable"),
        // Not part of OAuth2 spec, but used by some banks:
        LOGIN_REQUIRED("login_required"),
        CANCELED_BY_USER("action_canceled_by_user"),
        INVALID_AUTHENTICATION("invalid_authentication"),
        USER_CANCELED_AUTHORIZATION("USER-CANCELED-AUTHORIZATION");

        private final String value;

        ErrorType(String value) {
            this.value = value;
        }

        public String getValue() {
            return this.value;
        }

        public static ErrorType getErrorType(String value) {
            return Stream.of(values())
                    .filter(v -> value.equals(v.getValue()))
                    .findAny()
                    .orElse(UNKNOWN);
        }
    }
}
