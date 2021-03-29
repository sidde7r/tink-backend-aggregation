package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants;
import se.tink.backend.aggregation.agents.utils.crypto.hash.Hash;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@Slf4j
public class LocalCertificateTlsConfigurationSetter implements TlsConfigurationSetter {

    private final TransportKey transportKey;

    public LocalCertificateTlsConfigurationSetter(
            String transportKey, String transportKeyPassword) {
        this.transportKey = new TransportKey(transportKey, transportKeyPassword);
    }

    @Override
    public void applyConfiguration(final TinkHttpClient client) {
        log.info(
                "P12TransportKey SHA1: {}",
                Optional.ofNullable(transportKey)
                        .map(TransportKey::getP12Key)
                        .map(Hash::sha1AsHex)
                        .orElse(null));
        client.trustRootCaCertificate(
                UkOpenBankingV31Constants.UKOB_ROOT_CA_JKS,
                UkOpenBankingV31Constants.UKOB_ROOT_CA_JKS_PASSWORD);
        client.setSslClientCertificate(transportKey.getP12Key(), transportKey.getPassword());
    }
}
