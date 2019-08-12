package se.tink.backend.aggregation.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;

@JsonObject
public class EidasProxyConfiguration {
    private String host;
    private String caPath;
    private String tlsCrtPath;
    private String tlsKeyPath;
    private boolean localEidasDev;

    public EidasProxyConfiguration() {}

    public static EidasProxyConfiguration createLocal(final String host) {
        final EidasProxyConfiguration configuration = new EidasProxyConfiguration();
        configuration.host = host;
        configuration.localEidasDev = true;
        return configuration;
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

    public InternalEidasProxyConfiguration toInternalConfig() {
        return new InternalEidasProxyConfiguration(
                host, caPath, tlsCrtPath, tlsKeyPath, localEidasDev);
    }
}
