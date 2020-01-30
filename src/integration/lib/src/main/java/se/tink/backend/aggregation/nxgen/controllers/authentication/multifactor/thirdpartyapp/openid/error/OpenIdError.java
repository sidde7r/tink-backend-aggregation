package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.error;

public class OpenIdError {
    private String errorType;
    private String errorMessage;

    private OpenIdError(String errorType, String errorMessage) {
        this.errorType = errorType;
        this.errorMessage = errorMessage;
    }

    public static OpenIdError create(String errorType, String errorMessage) {
        return new OpenIdError(errorType, errorMessage);
    }

    public String getErrorType() {
        return errorType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
