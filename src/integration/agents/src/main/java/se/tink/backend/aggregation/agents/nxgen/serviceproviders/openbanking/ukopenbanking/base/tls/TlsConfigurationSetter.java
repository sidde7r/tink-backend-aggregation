package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.tls;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public interface TlsConfigurationSetter {
    void applyConfiguration(final TinkHttpClient client);
}
