package se.tink.backend.integration.tpp_secrets_service.client;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TppSecretsServiceConfiguration {

    private String host;
    private int port;
    private String caPath;
    private String tlsCrtPath;
    private String tlsKeyPath;
    private boolean localTppSecretsDev;

    public boolean isLocalTppSecretsDev() {
        return localTppSecretsDev;
    }

    public String getCaPath() {
        return caPath;
    }

    public String getTlsCrtPath() {
        return tlsCrtPath;
    }

    public String getTlsKeyPath() {
        return tlsKeyPath;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
