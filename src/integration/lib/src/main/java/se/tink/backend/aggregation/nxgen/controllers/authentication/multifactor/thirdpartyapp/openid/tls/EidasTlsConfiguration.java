package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls;

import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class EidasTlsConfiguration implements TlsConfigurationAdapter {

    private static final String EIDAS_PROXY_URL =
            "https://eidas-proxy.staging.aggregation.tink.network";

    @Override
    public TinkHttpClient applyConfiguration(final TinkHttpClient client) {
        client.setEidasProxy(EidasProxyConfiguration.createLocal(EIDAS_PROXY_URL));
        client.setEidasIdentity(
                new EidasIdentity("oxford-staging", "5f98e87106384b2981c0354a33b51590", ""));

        return client;
    }
}
