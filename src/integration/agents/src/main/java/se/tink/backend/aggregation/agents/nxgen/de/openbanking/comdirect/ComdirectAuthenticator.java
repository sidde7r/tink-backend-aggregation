package se.tink.backend.aggregation.agents.nxgen.de.openbanking.comdirect;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ComdirectAuthenticator extends Xs2aDevelopersAuthenticator {

    public ComdirectAuthenticator(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration xs2ADevelopersProviderConfiguration,
            LocalDateTimeSource localDateTimeSource,
            Credentials credentials) {
        super(
                apiClient,
                persistentStorage,
                xs2ADevelopersProviderConfiguration,
                localDateTimeSource,
                credentials,
                true);
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        URL result = super.buildAuthorizeUrl(state);
        return new URL(
                result.get()
                        .replace(
                                ComdirectConstants.PSD_URI_TO_BE_REPLACED,
                                ComdirectConstants.XS2A_URI_REPLACEMENT));
    }
}
