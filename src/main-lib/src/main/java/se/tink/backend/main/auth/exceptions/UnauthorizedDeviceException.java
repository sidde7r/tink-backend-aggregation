package se.tink.backend.main.auth.exceptions;

public class UnauthorizedDeviceException extends RuntimeException {
    private final String mfaUrl;

    public UnauthorizedDeviceException(String mfaUrl) {
        this.mfaUrl = mfaUrl;
    }

    public String getMfaUrl() {
        return mfaUrl;
    }
}
