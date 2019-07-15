package se.tink.backend.aggregation.register.nl.bunq;

public class BunqRegistrationResponse {
    private String clientId;
    private String clientSecret;
    private String psd2ApiKey;
    private String psd2ClientAuthToken;
    private String psd2InstallationKeyPair;
    private String redirectUrl;

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    public void setPsd2ClientAuthToken(String psd2ClientAuthToken) {
        this.psd2ClientAuthToken = psd2ClientAuthToken;
    }

    public void setPsd2InstallationKeyPair(String psd2InstallationKeyPair) {
        this.psd2InstallationKeyPair = psd2InstallationKeyPair;
    }

    public void setPsd2ApiKey(String psd2ApiKey) {
        this.psd2ApiKey = psd2ApiKey;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String toString() {
        return String.join(
                System.lineSeparator(),
                String.format("clientId : \"%s\"", clientId),
                String.format("clientSecret : \"%s\"", clientSecret),
                String.format("psd2ApiKey : \"%s\"", psd2ApiKey),
                String.format("psd2ClientAuthToken : '%s'", psd2ClientAuthToken),
                String.format("psd2InstallationKeyPair : '%s'", psd2InstallationKeyPair),
                String.format("redirectUrl : \"%s\"", redirectUrl));
    }
}
