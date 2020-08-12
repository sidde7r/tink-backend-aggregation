package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.identitydata;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.libraries.identitydata.IdentityData;

@AllArgsConstructor
public class IdentityDataFetcher {

    private FetcherClient fetcherClient;

    public IdentityData fetchIdentityData() {
        IdentityDataResponse identityDataResponse = fetcherClient.fetchIdentityData();

        return IdentityData.builder()
                .addFirstNameElement(identityDataResponse.getFirstName())
                .addSurnameElement(identityDataResponse.getLastName())
                .setDateOfBirth(identityDataResponse.getBirthDate())
                .build();
    }
}
