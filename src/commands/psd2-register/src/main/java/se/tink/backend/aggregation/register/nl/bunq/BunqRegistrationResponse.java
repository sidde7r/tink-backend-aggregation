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

        final String sb =
                String.format("clientId : \"%s\"", clientId)
                        + System.lineSeparator()
                        + String.format("clientSecret : \"%s\"", clientSecret)
                        + System.lineSeparator()
                        + String.format("psd2ApiKey : \"%s\"", psd2ApiKey)
                        + System.lineSeparator()
                        + String.format("psd2ClientAuthToken : '%s'", psd2ClientAuthToken)
                        + System.lineSeparator()
                        + String.format("psd2InstallationKeyPair : '%s'", psd2InstallationKeyPair)
                        + System.lineSeparator()
                        + String.format("redirectUrl : \"%s\"", redirectUrl);
        return sb;
    }
}
