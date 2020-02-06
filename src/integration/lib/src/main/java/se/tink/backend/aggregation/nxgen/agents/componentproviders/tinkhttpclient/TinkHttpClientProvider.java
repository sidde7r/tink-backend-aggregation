package se.tink.backend.aggregation.nxgen.agents.componentproviders.tinkhttpclient;

import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public interface TinkHttpClientProvider {
    TinkHttpClient getTinkHttpClient();
}
