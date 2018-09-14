package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.http.URL;

public abstract class PagableResponse extends BaseResponse {

    public abstract Optional<URL> getPaginationKey();
}
