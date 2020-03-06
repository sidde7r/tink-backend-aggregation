package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

public interface AccessTokenFetcher {

    AccessTokenStatus getAccessTokenStatus();

    AccessTokenRefreshStatus refreshAccessToken();

    void retrieveAccessToken();
}
