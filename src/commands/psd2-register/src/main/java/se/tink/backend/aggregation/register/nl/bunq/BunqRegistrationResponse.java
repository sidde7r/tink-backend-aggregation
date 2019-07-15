package se.tink.backend.aggregation.register.nl.bunq;

public class BunqRegistrationResponse {
    private String clientId;
    private String clientSecret;
    private String psd2ApiKey;
    private String psd2ClientAuthToken;
    private String psd2InstallationKeyPair;
    private String redirectUrl;

    private BunqRegistrationResponse(final Builder builder) {
        clientId = builder.clientId;
        clientSecret = builder.clientSecret;
        psd2ApiKey = builder.psd2ApiKey;
        psd2ClientAuthToken = builder.psd2ClientAuthToken;
        psd2InstallationKeyPair = builder.psd2InstallationKeyPair;
        redirectUrl = builder.redirectUrl;
    }

    public static BunqRegistrationResponse.Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String clientId;
        private String clientSecret;
        private String psd2ApiKey;
        private String psd2ClientAuthToken;
        private String psd2InstallationKeyPair;
        private String redirectUrl;

        private Builder() {}

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

        public BunqRegistrationResponse build() {
            return new BunqRegistrationResponse(this);
        }
    }

    public String toYaml() {
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
