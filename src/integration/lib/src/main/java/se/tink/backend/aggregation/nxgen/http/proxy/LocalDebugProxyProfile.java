package se.tink.backend.aggregation.nxgen.http.proxy;

import java.util.Optional;

/**
 * Tunnel all traffic through a local proxy, e.g. Charles Proxy, in order to inspect or modify the
 * requests/responses. The `uri` is hardcoded to `http://127.0.0.1` with a variable port.
 */
public class LocalDebugProxyProfile implements ProxyProfile {
    private final int port;

    public LocalDebugProxyProfile(int port) {
        this.port = port;
    }

    @Override
    public Optional<String> getUri() {
        return Optional.of(String.format("http://127.0.0.1:%d", this.port));
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
        return true;
    }
}
