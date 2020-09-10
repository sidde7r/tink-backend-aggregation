package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.universo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UniversoApiClient extends Xs2aDevelopersApiClient {
    private static final String API_KEY = "apiKey";

    private final UniversoProviderConfiguration configuration;

    public UniversoApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            UniversoProviderConfiguration configuration) {
        super(client, persistentStorage, configuration);
        this.configuration = configuration;
    }

    @Override
    protected RequestBuilder createRequest(URL url) {
        RequestBuilder requestBuilder = super.createRequest(url);
        return requestBuilder.header(API_KEY, configuration.getApiKey());
    }
}
