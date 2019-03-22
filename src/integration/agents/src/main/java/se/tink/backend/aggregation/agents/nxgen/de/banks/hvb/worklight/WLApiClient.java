package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.worklight;

import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public final class WLApiClient {
    private final TinkHttpClient client;

    public WLApiClient(final TinkHttpClient client) {
        this.client = client;
    }

    // TODO replace with an API of thin HttpRequest/HttpResponse methods
    public TinkHttpClient getClient() {
        return client;
    }
}
