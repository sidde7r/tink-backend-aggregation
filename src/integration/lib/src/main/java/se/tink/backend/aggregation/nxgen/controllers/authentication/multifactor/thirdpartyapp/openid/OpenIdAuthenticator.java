package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import se.tink.backend.aggregation.nxgen.http.URL;

public interface OpenIdAuthenticator {
    // The authorize url will be sent to the front-end and opened it a web browser.
    // The agent can attach any non-standard OpenId parameters/features to the URL at this point.
    URL decorateAuthorizeUrl(URL authorizeUrl, String state, String nonce);
}
