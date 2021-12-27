package se.tink.backend.aggregation.nxgen.http.proxy;

import java.util.Optional;
import javax.annotation.Nullable;

public final class AuthenticatedProxyProfile implements ProxyProfile {
    @Nullable private final String uri;
    @Nullable private final String username;
    @Nullable private final String password;

    public AuthenticatedProxyProfile(
            @Nullable String uri, @Nullable String username, @Nullable String password) {
        this.uri = uri;
        this.username = username;
        this.password = password;
    }

    @Override
    public Optional<String> getUri() {
        return Optional.ofNullable(this.uri);
    }

    @Override
    public Optional<String> getUsername() {
        return Optional.ofNullable(this.username);
    }

    @Override
    public Optional<String> getPassword() {
        return Optional.ofNullable(this.password);
    }

    @Override
    public boolean shouldDisableSslVerification() {
        return false;
    }
}
