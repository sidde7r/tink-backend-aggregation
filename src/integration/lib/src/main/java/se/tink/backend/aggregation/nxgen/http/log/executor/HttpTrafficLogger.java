package se.tink.backend.aggregation.nxgen.http.log.executor;

import java.util.Optional;

public interface HttpTrafficLogger {

    boolean isEnabled();

    Optional<String> tryGetLogContent();
}
