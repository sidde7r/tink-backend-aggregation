package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.session;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SkandiaBankenSessionHandler implements SessionHandler {

    private final SkandiaBankenApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SkandiaBankenSessionHandler(
            SkandiaBankenApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void logout() {
        // NOP
    }

    @Override
    public void keepAlive() throws SessionException {
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (oAuth2Token.hasAccessExpired()) {
            persistentStorage.remove(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN);

            String refreshToken =
                    oAuth2Token
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            oAuth2Token = getOAuth2Token(refreshToken);

            if (!oAuth2Token.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            persistentStorage.put(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
        }
    }

    private OAuth2Token getOAuth2Token(String refreshToken) throws SessionException {
        try {
            return apiClient.refreshToken(refreshToken).toOAuth2Token();
        } catch (HttpResponseException hre) {
            HttpResponse response = hre.getResponse();

            if (response.getStatus() == HttpStatus.SC_UNAUTHORIZED) {
                throw SessionError.SESSION_EXPIRED.exception(hre);
            }

            if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                    || response.getBody(ErrorResponse.class).isUnauthorized()) {
                throw SessionError.SESSION_EXPIRED.exception(hre);
            }
            throw hre;
        }
    }
}
