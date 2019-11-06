package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface SamlinkConfiguration extends ClientConfiguration {

    URL build(String path);

    String getClientApp();

    boolean isV2();
}
