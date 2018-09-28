package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.authenticator;

import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingAuthenticator implements OpenIdAuthenticator {
    @Override
    public URL buildAuthorizeUrl(URL authorizeUrl) {
        return authorizeUrl;
    }
}
