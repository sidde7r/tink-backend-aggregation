package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher.entity.IdentityDataEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class IdentityFetcher implements IdentityDataFetcher {

    private final IspApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchAccountsAndIdentities().getPayload().getAccountsAndIdentitiesViews()
                .stream()
                .flatMap(accountViewEntity -> accountViewEntity.getIdentities().stream())
                .map(IdentityDataEntity::toTinkIdentityData)
                .distinct()
                .reduce(IdentityData::throwingMerger)
                .orElseThrow(IllegalArgumentException::new);
    }
}
