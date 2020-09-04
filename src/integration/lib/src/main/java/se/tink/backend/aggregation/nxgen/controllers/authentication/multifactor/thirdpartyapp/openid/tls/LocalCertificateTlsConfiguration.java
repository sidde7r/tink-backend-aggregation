package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.TransportKey;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class LocalCertificateTlsConfiguration implements TlsConfigurationOverride {

    private TransportKey transportKey;

    public LocalCertificateTlsConfiguration(String transportKey, String transportKeyPassword) {
        this(new TransportKey(transportKey, transportKeyPassword));
    }

    public LocalCertificateTlsConfiguration(TransportKey transportKey) {
        this.transportKey = transportKey;
    }

    @Override
    public void applyConfiguration(final TinkHttpClient client) {
        client.setSslClientCertificate(transportKey.getP12Key(), transportKey.getPassword());
    }
}
