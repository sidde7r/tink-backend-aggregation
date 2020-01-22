package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import java.util.Collections;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class CreditAgricoleBaseIdentityDataFetcher implements IdentityDataFetcher {

    private final CreditAgricoleBaseApiClient apiClient;

    public CreditAgricoleBaseIdentityDataFetcher(CreditAgricoleBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        apiClient.putConsents(Collections.emptyList());
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
