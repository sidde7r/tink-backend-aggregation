package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface PolishAccountsApiUrlFactory {
    URL getAccountsUrl();

    URL getAccountDetailsUrl(String accountNumber);
}
