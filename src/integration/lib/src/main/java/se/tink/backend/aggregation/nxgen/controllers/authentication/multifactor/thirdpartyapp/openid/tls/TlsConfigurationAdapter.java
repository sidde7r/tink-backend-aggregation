package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.tls;

import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public interface TlsConfigurationAdapter {
    TinkHttpClient applyConfiguration(final TinkHttpClient client);
}
