package se.tink.backend.aggregation.nxgen.controllers.authentication.oauth;

import java.util.Map;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SupplementalWaitRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;

public interface OAuth2AuthorizationServerClient {

    ThirdPartyAppAuthenticationPayload getAuthorizationEndpointPayload();

    SupplementalWaitRequest getWaitingForResponseConfiguration();

    OAuth2Token handleAuthorizationResponse(Map<String, String> callbackData)
            throws OAuth2AuthorizationException;

    OAuth2Token refreshAccessToken(final String refreshToken) throws OAuth2AuthorizationException;
}
