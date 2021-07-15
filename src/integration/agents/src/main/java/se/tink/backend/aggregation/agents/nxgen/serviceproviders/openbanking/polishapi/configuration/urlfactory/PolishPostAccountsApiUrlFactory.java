package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class PolishPostAccountsApiUrlFactory implements PolishAccountsApiUrlFactory {

    private final URL baseUrl;
    private final String version;

    private URL getBaseAccountsUrl() {
        return baseUrl.concatWithSeparator(version)
                .concatWithSeparator("accounts")
                .concatWithSeparator(version);
    }

    @Override
    public URL getAccountsUrl() {
        return getBaseAccountsUrl().concatWithSeparator("getAccounts");
    }

    @Override
    public URL getAccountDetailsUrl(String accountNumber) {
        return getBaseAccountsUrl().concatWithSeparator("getAccount");
    }
}
