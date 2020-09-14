package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.entities.CustomerBodyEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class IcaBankenIdentityDataFetcher implements IdentityDataFetcher {

    private final IcaBankenApiClient apiClient;

    public IcaBankenIdentityDataFetcher(IcaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        final CustomerBodyEntity customer = apiClient.fetchCustomer();

        return SeIdentityData.of(
                customer.getFirstName(),
                customer.getLastName(),
                customer.getPersonalIdentityNumber());
    }

    public FetchIdentityDataResponse getIdentityDataResponse() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
