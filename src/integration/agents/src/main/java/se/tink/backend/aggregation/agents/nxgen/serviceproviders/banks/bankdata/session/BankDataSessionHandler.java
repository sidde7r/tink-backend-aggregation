package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.session;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.FormKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc.OAuthResponse;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@AllArgsConstructor
public class BankDataSessionHandler implements SessionHandler {
    private final BankDataApiClient bankDataApiClient;
    private final BankDataPersistentStorage jyskePersistentStorage;
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
                        bankDataApiClient.fetchAccessToken(clientId, clientSecret, oauthForm);
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
