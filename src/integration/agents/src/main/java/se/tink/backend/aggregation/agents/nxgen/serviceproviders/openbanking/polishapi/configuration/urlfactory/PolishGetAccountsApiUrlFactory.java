package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class PolishGetAccountsApiUrlFactory implements PolishAccountsApiUrlFactory {

    private final URL baseUrl;
    private final String apiType;
    private final String version;

    private URL getBaseAccountsUrl() {
        return baseUrl.concatWithSeparator(apiType)
                .concatWithSeparator(version)
                .concatWithSeparator("accounts");
    }

    @Override
    public URL getAccountsUrl() {
        return getBaseAccountsUrl();
    }

    @Override
    public URL getAccountDetailsUrl(String accountNumber) {
        return getAccountsUrl().concatWithSeparator(accountNumber);
    }
}
