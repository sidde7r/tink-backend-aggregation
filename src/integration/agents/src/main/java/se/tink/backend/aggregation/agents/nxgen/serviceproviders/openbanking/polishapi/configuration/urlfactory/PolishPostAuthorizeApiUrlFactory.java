package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.urlfactory;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class PolishPostAuthorizeApiUrlFactory implements PolishAuthorizeApiUrlFactory {

    private final URL baseUrl;
    private final String version;

    private URL getBaseAuthUrl() {
        return baseUrl.concatWithSeparator(version)
                .concatWithSeparator("auth")
                .concatWithSeparator(version);
    }

    @Override
    public URL getAuthorizeUrl() {
        return getBaseAuthUrl().concatWithSeparator("authorize");
    }

    @Override
    public URL getOauth2TokenUrl() {
        return getBaseAuthUrl().concatWithSeparator("token");
    }
}
