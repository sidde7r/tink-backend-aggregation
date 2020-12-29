package se.tink.backend.aggregation.nxgen.core.authentication;

public interface OAuth2TokenAccessor {

    void invalidate();

    OAuth2Token getAccessToken();
}
