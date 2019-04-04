package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class IngConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String clientId;
    private String clientSigningKeyPath;
    private String clientSigningCertificatePath;
    private String clientKeyStorePath;
    private String clientKeyStorePassword;
    private String redirectUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSigningKeyPath() {
        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        return clientSigningCertificatePath;
    }

    public String getClientKeyStorePath() {
        return clientKeyStorePath;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getClientKeyStorePassword() {
        return clientKeyStorePassword;
    }
}
