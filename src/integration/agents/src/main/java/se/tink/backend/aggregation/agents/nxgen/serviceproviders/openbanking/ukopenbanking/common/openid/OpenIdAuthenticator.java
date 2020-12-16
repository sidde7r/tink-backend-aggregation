package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface OpenIdAuthenticator {

    URL decorateAuthorizeUrl(URL authorizeUrl, String state, String nonce, String callbackUri);
}
