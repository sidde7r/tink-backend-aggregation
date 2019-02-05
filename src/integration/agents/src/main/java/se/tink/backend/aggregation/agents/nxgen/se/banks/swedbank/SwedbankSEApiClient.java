package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinkEntity;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SwedbankSEApiClient extends SwedbankDefaultApiClient {
    public SwedbankSEApiClient(TinkHttpClient client,
            SwedbankConfiguration configuration,
            String username, SessionStorage sessionStorage) {
        super(client, configuration, username, sessionStorage);
    }

    public DetailedLoanResponse loadDetailsEntity(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedLoanResponse.class);
    }

    public LinkEntity loadLinkEntity(LinkEntity linkEntity) {
        return makeRequest(linkEntity, LinkEntity.class);
    }
}
