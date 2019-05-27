package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import se.tink.backend.aggregation.nxgen.http.URL;

public interface SamlinkConfiguration {

    URL build(String path);

    String getClientApp();

    boolean isV2();
}
