package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class VolksbankHttpClient {

    private TinkHttpClient tinkClient;

    public VolksbankHttpClient(TinkHttpClient client) {

        this.tinkClient = client;
    }

    public TinkHttpClient getTinkClient() {
        return tinkClient;
    }
}
