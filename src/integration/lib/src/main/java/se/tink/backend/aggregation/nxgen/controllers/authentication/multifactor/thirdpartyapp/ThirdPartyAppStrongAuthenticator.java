package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public interface ThirdPartyAppStrongAuthenticator<T> {

    ThirdPartyAppResponse<T> init();

    ThirdPartyAppResponse<T> collect(final Map<String, String> callbackData)
        throws AuthenticationException, AuthorizationException;

    ThirdPartyAppAuthenticationPayload getAppPayload();

    String getStrongAuthenticationStateSupplementalKey();

    long getWaitForMinutes();

}
