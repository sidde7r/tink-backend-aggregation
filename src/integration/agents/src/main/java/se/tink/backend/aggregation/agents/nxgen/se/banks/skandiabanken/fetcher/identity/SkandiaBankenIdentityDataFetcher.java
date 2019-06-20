package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.identity;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.identity.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class SkandiaBankenIdentityDataFetcher implements IdentityDataFetcher {
    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenIdentityDataFetcher(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        final IdentityDataResponse identityDataResponse = apiClient.fetchIdentityData();

        return SeIdentityData.of(
                identityDataResponse.getFullname(),
                identityDataResponse.getNationalIdentificationNumber());
    }

    public FetchIdentityDataResponse getIdentityDataResponse() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
