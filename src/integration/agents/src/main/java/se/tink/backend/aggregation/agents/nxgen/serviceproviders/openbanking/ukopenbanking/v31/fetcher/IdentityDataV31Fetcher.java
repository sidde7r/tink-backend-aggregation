package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.rpc.IdentityDataV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.IdentityDataFetcher;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class IdentityDataV31Fetcher implements IdentityDataFetcher {

    private final UkOpenBankingApiClient ukOpenBankingApiClient;
    private static final AggregationLogger log =
            new AggregationLogger(IdentityDataV31Fetcher.class);

    public IdentityDataEntity fetchUserDetails(URL identityDataEndpointURL) {

        try {
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
        } catch (HttpResponseException e) {
            /* FIXME
            Monzo API returns us 403 even though we put correct scopes in token request. Probably
            the bank does not follow the protocol and expects another scope. We need to investigate
            that. Meanwhile, to prevent the agent from getting crashed we will gracefully handle
            the error message from Monzo here
             */
            if (e.getResponse().getStatus() == 403) {
                log.info("Failed to fetch identity data, bank API responded with 403");
                return null;
            }
            // FIXME probably exception should be rethrown
        }
        return null;
    }
}
