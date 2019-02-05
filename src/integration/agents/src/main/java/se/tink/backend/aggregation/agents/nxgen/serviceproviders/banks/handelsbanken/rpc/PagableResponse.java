package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface PagableResponse {
    Optional<URL> getPaginationKey();
}
