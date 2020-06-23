package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class CmcicIdentityDataFetcher implements IdentityDataFetcher {

    private final CmcicApiClient apiClient;

    public CmcicIdentityDataFetcher(CmcicApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        final EndUserIdentityResponse endUserIdentityResponse = apiClient.getEndUserIdentity();
        return IdentityData.builder()
                .setFullName(endUserIdentityResponse.getConnectedPsu())
                .setDateOfBirth(null)
                .build();
    }

    public FetchIdentityDataResponse response() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
