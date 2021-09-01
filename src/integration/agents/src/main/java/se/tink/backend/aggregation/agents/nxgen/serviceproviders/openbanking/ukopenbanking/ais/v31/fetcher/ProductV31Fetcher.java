package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.ProductV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.ProductFetcher;

public class ProductV31Fetcher implements ProductFetcher {

    private final UkOpenBankingApiClient apiClient;

    public ProductV31Fetcher(UkOpenBankingApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Optional<ProductV31Response> fetchProduct(String accountId) {
        return apiClient.fetchV31Product(accountId);
    }
}
