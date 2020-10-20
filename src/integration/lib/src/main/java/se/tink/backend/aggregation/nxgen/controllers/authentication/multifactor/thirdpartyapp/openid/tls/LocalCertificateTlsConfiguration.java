package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.TransportKey;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@Slf4j
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
        log.info(
                "P12TransportKey SHA1: {}",
                Optional.ofNullable(transportKey)
                        .map(TransportKey::getP12Key)
                        .map(Hash::sha1AsHex)
                        .orElse(null));
        client.setSslClientCertificate(transportKey.getP12Key(), transportKey.getPassword());
    }
}
