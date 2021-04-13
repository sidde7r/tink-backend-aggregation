package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.configuration;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface UkOpenBankingPisConfig {

    URL getWellKnownURL();

    String getBaseUrl();

    boolean useMaxAge();
}
