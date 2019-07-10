package se.tink.backend.aggregation.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class EidasProxyConfiguration {
    private String host;
    private String caPath;
    private String tlsCrtPath;
    private String tlsKeyPath;
    private boolean localEidasDev;

    public EidasProxyConfiguration() {}

    public EidasProxyConfiguration(String host, boolean localEidasDev) {
        this.host = host;
        this.localEidasDev = localEidasDev;
    }

    public String getHost() {
        return host;
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

    public boolean isLocalEidasDev() {
        return localEidasDev;
    }
}
