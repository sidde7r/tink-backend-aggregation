package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.filter.SwedbankSeHttpFilter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.loan.rpc.DetailedLoanResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class SwedbankSEApiClient extends SwedbankDefaultApiClient {
    public SwedbankSEApiClient(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            String username,
            SwedbankStorage swedbankStorage) {
        super(client, configuration, username, swedbankStorage);
        this.client.addFilter(new SwedbankSeHttpFilter());
    }

    public DetailedLoanResponse loadDetailsEntity(LinkEntity linkEntity) {
        return makeRequest(linkEntity, DetailedLoanResponse.class, true);
    }
}
