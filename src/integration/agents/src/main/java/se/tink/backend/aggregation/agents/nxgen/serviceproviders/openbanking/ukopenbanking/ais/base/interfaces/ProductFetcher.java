package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.fetcher.rpc.ProductV31Response;

public interface ProductFetcher {

    Optional<ProductV31Response> fetchProduct(String accountId);
}
