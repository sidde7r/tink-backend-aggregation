package se.tink.backend.aggregation.configuration.eidas.proxy;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.eidas.InternalEidasProxyConfiguration;

@JsonObject
public class EidasProxyConfiguration {
    private String host;
    private String caPath;
    private String tlsCrtPath;
    private String tlsKeyPath;
    private String environment;
    private boolean localEidasDev;
    private boolean useEidasProxyQsealcSignerHttpClient = false;

    public EidasProxyConfiguration() {}

    public static EidasProxyConfiguration createLocal(final String host) {
        final EidasProxyConfiguration configuration = new EidasProxyConfiguration();
        configuration.host = host;
        configuration.localEidasDev = true;
        return configuration;
    }

    @Deprecated
    public String getHost() {
        return host;
    }

    public boolean isUseEidasProxyQsealcSignerHttpClient() {
        return useEidasProxyQsealcSignerHttpClient;
    }

    public InternalEidasProxyConfiguration toInternalConfig() {
        return new InternalEidasProxyConfiguration(
                host,
                caPath,
                tlsCrtPath,
                tlsKeyPath,
                environment,
                localEidasDev,
                useEidasProxyQsealcSignerHttpClient);
    }
}
