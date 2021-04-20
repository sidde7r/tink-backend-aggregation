package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers;

import java.time.Instant;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.TokenResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2TokenStorage;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2based.AccessTokenStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@RequiredArgsConstructor
public class AktiaAccessTokenRetriever {

    final AktiaApiClient aktiaApiClient;
    final OAuth2TokenStorage tokenStorage;

    public AccessTokenStatus getStatusFromStorage() {
        return tokenStorage
                .getToken()
                .map(AktiaAccessTokenRetriever::getTokenStatus)
                .orElse(AccessTokenStatus.NOT_PRESENT);
    }

    public void getFromRequestAndStore(AuthenticationRequest request) {
        final Credentials credentials = request.getCredentials();
        final String username = credentials.getField(Field.Key.USERNAME);
        final String password = credentials.getField(Field.Key.PASSWORD);

        final TokenResponseDto tokenResponse =
                aktiaApiClient.retrieveAccessToken(username, password);
        final OAuth2Token oAuth2Token = convertResponseToOauth2Token(tokenResponse);

        tokenStorage.storeToken(oAuth2Token);
        request.getCredentials()
                .setSessionExpiryDate(
                        Instant.ofEpochMilli(oAuth2Token.getAccessExpireEpoch() * 1000)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate());
    }

    private static AccessTokenStatus getTokenStatus(OAuth2Token token) {
        return token.isValid() ? AccessTokenStatus.VALID : AccessTokenStatus.EXPIRED;
    }

    private static OAuth2Token convertResponseToOauth2Token(TokenResponseDto response) {
        return OAuth2Token.create(
                response.getTokenType(), response.getAccessToken(), null, response.getExpiresIn());
    }
}
