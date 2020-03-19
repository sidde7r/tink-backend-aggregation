package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.Arrays;

public enum OAuth2AuthorizationErrorType {
    INVALID_REQUEST("invalid_request"),
    UNAUTHORIZED_CLIENT("unauthorized_client"),
    ACCESS_DENIED("access_denied"),
    UNSUPPORTED_RESPONSE_TYPE("unsupported_response_type"),
    INVALID_SCOPE("invalid_scope"),
    SERVER_ERROR("server_error"),
    TEMPORARILY_UNAVAILABLE("temporarily_unavailable"),
    CUSTOM("");

    private String code;

    OAuth2AuthorizationErrorType(String code) {
        this.code = code;
    }

    String getCode() {
        return code;
    }

    static OAuth2AuthorizationErrorType getByCode(final String code) {
        return Arrays.stream(values()).filter(v -> code.equals(v.code)).findAny().orElse(CUSTOM);
    }
}
