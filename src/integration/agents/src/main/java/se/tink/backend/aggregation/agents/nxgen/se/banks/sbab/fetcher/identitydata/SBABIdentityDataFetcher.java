package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.identitydata;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SBABApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc.AccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class SBABIdentityDataFetcher implements IdentityDataFetcher {
    private final SBABApiClient apiClient;

    public SBABIdentityDataFetcher(SBABApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        final AccountsResponse accountsResponse = apiClient.fetchAccounts();
        final String firstName = accountsResponse.getCallingUser().getFirstName();
        final String lastName = accountsResponse.getCallingUser().getLastName();
        final String ssn = accountsResponse.getCallingUser().getPersonalIdentityNumber();

        return SeIdentityData.of(firstName, lastName, ssn);
    }

    public FetchIdentityDataResponse getIdentityDataResponse() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
