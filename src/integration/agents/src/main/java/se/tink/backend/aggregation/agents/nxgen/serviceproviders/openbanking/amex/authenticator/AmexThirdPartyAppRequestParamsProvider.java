package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.amex.AmexApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.OAuth2ThirdPartyAppRequestParamsProvider;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class AmexThirdPartyAppRequestParamsProvider
        implements OAuth2ThirdPartyAppRequestParamsProvider {

    private final AmexApiClient amexApiClient;

    @Override
    public URL getAuthorizeUrl(String state) {
        return amexApiClient.getAuthorizeUrl(state);
    }

    @Override
    public String getCallbackDataAuthCodeKey() {
        return "authtoken";
    }
}
