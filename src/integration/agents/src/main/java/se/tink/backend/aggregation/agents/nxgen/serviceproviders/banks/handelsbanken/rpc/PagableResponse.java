package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface PagableResponse {
    Optional<URL> getPaginationKey();
}
