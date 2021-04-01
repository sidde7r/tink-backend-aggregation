package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.tls;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AllArgsConstructor
public class EidasProxyTlsConfigurationSetter implements TlsConfigurationSetter {

    private final AgentsServiceConfiguration configuration;

    @Override
    public void applyConfiguration(final TinkHttpClient client) {
        client.setEidasProxy(configuration.getEidasProxy());
    }
}
