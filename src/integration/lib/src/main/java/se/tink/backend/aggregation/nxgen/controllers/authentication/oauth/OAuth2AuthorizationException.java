package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

public class OAuth2AuthorizationException extends RuntimeException {

    private OAuth2AuthorizationErrorType errorType;

    private String errorRawCode;

    private String description;

    public OAuth2AuthorizationException(String errorRawCode, String description) {
        super("[" + errorRawCode + "] " + description);
        this.errorType = OAuth2AuthorizationErrorType.getByCode(errorRawCode);
        this.errorRawCode = errorRawCode;
        this.description = description;
    }

    public OAuth2AuthorizationException(
            OAuth2AuthorizationErrorType errorType, String description) {
        super("[" + errorType.getCode() + "] " + description);
        this.errorType = errorType;
        this.errorRawCode = errorType.getCode();
        this.description = description;
    }

    public OAuth2AuthorizationException(
            Throwable cause, OAuth2AuthorizationErrorType errorType, String description) {
        super(cause);
        this.errorType = errorType;
        this.description = description;
    }

    public OAuth2AuthorizationErrorType getErrorType() {
        return errorType;
    }

    public String getErrorRawCode() {
        return errorRawCode;
    }

    public String getDescription() {
        return description;
    }
}
