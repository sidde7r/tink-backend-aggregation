package se.tink.backend.aggregation.nxgen.http.proxy;

import java.util.Optional;

/**
 * This ProxyProfile is inert. It will, by returning `Optional.empty()`, not configure any proxy in
 * the http client.
 */
public class NoopProxyProfile implements ProxyProfile {
    @Override
    public Optional<String> getUri() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getUsername() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.empty();
    }

    @Override
    public boolean shouldDisableSslVerification() {
        return false;
    }
}
