package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.session;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.authenticator.rpc.OAuthResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class JyskeBankSessionHandler implements SessionHandler {
    private final JyskeBankApiClient jyskeBankApiClient;
    private final JyskeBankPersistentStorage jyskePersistentStorage;
    private final SessionStorage sessionStorage;

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            if (sessionStorage.containsKey(Storage.REFRESH_TOKEN)) {
                final String clientId = jyskePersistentStorage.getClientId();
                final String clientSecret = jyskePersistentStorage.getClientSecret();
                final Form oauthForm =
                        Form.builder()
                                .put(FormKeys.GRANT_TYPE, FormValues.REFRESH_TOKEN)
                                .put(
                                        FormKeys.REFRESH_TOKEN,
                                        sessionStorage.get(Storage.REFRESH_TOKEN))
                                .build();
                final OAuthResponse oAuthResponse =
                        jyskeBankApiClient.fetchAccessToken(clientId, clientSecret, oauthForm);
                sessionStorage.put(Storage.ACCESS_TOKEN, oAuthResponse.getAccessToken());
                sessionStorage.put(Storage.REFRESH_TOKEN, oAuthResponse.getRefreshToken());
            } else {
                throw SessionError.SESSION_EXPIRED.exception();
            }
        } catch (RuntimeException e) {
            throw SessionError.SESSION_EXPIRED.exception(e);
        }
    }
}
