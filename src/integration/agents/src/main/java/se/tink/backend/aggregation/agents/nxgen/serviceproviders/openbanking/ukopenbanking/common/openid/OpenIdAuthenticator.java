package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface OpenIdAuthenticator {
    // The authorize url will be sent to the front-end and opened it a web browser.
    // The agent can attach any non-standard OpenId parameters/features to the URL at this point.

    ClientMode getClientCredentialScope();

    URL decorateAuthorizeUrl(URL authorizeUrl, String state, String nonce, String callbackUri);
}
