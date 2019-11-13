package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls;

import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public interface TlsConfigurationOverride {
    void applyConfiguration(final TinkHttpClient client);
}
