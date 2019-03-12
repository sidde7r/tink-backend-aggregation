package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;

public class SpareBank1Authenticator implements Authenticator {
    private final SpareBank1ApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public SpareBank1Authenticator(
            SpareBank1ApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {

        GetTokenForm form =
                GetTokenForm.builder()
                        .setGrantType(SpareBank1Constants.FormValues.GRANT_TYPE)
                        .setClientId(
                                persistentStorage.get(SpareBank1Constants.StorageKeys.CLIENT_ID))
                        .setClientSecret(
                                persistentStorage.get(
                                        SpareBank1Constants.StorageKeys.CLIENT_SECRET))
                        .build();

        sessionStorage.put(SpareBank1Constants.StorageKeys.TOKEN, apiClient.getToken(form));
    }
}
