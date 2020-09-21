package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount;

import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.DemobankApiClient;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.fetcher.transactionalaccount.entities.UserEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.EsIdentityData;

public class DemobankIdentityDataFetcher implements IdentityDataFetcher {

    private final DemobankApiClient apiClient;

    public DemobankIdentityDataFetcher(DemobankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {

        UserEntity userEntity = apiClient.fetchUser();

        return EsIdentityData.builder()
                .setNifNumber(userEntity.getSsn())
                .setFullName(userEntity.getName())
                .setDateOfBirth(userEntity.getDateOfBirth())
                .build();
    }
}
