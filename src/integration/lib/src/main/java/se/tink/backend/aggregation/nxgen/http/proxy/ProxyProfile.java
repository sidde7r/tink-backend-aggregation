package se.tink.backend.aggregation.nxgen.http.proxy;

import java.util.Optional;

public interface ProxyProfile {
    Optional<String> getUri();

    Optional<String> getUsername();

    Optional<String> getPassword();

    boolean shouldDisableSslVerification();
}
