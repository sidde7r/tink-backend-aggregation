package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.identity;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.rpc.MeResponse;

public class N26IdentityDataFetcher {

    private final N26ApiClient apiClient;

    public N26IdentityDataFetcher(final N26ApiClient apiClient) {
        this.apiClient = requireNonNull(apiClient);
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        return Optional.of(apiClient.fetchIdentityData())
                .map(MeResponse::toTinkIdentity)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
