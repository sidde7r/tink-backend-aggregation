package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.session;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.rpc.ErrorResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
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
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (oAuth2Token.hasAccessExpired()) {
            persistentStorage.remove(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN);

            String refreshToken =
                    oAuth2Token
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);
            oAuth2Token = getOAuth2Token(refreshToken);

            if (!oAuth2Token.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            persistentStorage.put(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, oAuth2Token);
        }
    }

    private OAuth2Token getOAuth2Token(String refreshToken) throws SessionException {
        try {
            return apiClient.refreshToken(refreshToken).toOAuth2Token();
        } catch (HttpResponseException hre) {
            if (hre.getResponse().getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR
                    || hre.getResponse().getBody(ErrorResponse.class).isUnauthorized()) {
                throw SessionError.SESSION_EXPIRED.exception(hre);
            }
            throw hre;
        }
    }
}
