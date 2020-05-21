package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class BnpParibasIdentityDataFetcher implements IdentityDataFetcher {

    private final BnpParibasApiBaseClient apiClient;

    public BnpParibasIdentityDataFetcher(BnpParibasApiBaseClient apiClient) {
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
