package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based;

import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface OAuth2ThirdPartyAppRequestParamsProvider {

    URL getAuthorizeUrl(String state);

    String getCallbackDataAuthCodeKey();
}
