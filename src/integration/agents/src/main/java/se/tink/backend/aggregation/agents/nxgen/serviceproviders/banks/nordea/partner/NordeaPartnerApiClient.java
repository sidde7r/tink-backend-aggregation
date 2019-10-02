package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.configuration.NordeaPartnerConfiguration;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordeaPartnerApiClient {

    private final TinkHttpClient client;
    private final SessionStorage sessionStorage;
    private NordeaPartnerConfiguration configuration;

    public NordeaPartnerApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        this.client = client;
        this.sessionStorage = sessionStorage;
    }

    public void setConfiguration(NordeaPartnerConfiguration configuration) {
        this.configuration = configuration;
    }
}
