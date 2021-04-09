package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls;

import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@Slf4j
public class FakeTlsConfigurationSetter implements TlsConfigurationSetter {

    @Override
    public void applyConfiguration(TinkHttpClient client) {
        log.info("Setting TLS configuration...");
    }
}
