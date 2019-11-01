package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.identity.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.identity.IdentityDataV31Response;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class IdentityDataV31Fetcher extends IdentityDataFetcher {

    private final UkOpenBankingApiClient ukOpenBankingApiClient;

    public IdentityDataV31Fetcher(UkOpenBankingApiClient ukOpenBankingApiClient) {
        super(ukOpenBankingApiClient);
        this.ukOpenBankingApiClient = ukOpenBankingApiClient;
    }

    @Override
    public IdentityDataEntity fetchUserDetails(URL identityDataEndpointURL) {
        IdentityDataV31Response response =
                ukOpenBankingApiClient
                        .createAisRequest(identityDataEndpointURL)
                        .get(IdentityDataV31Response.class);

        Optional<IdentityDataV31Entity> entity =
                response.getData().orElse(Collections.emptyList()).stream()
                        .filter(e -> e.getName() != null)
                        .findAny();

        if (entity.isPresent()) {
            return entity.get().toTinkIdentityData();
        }

        return null;
    };
}
