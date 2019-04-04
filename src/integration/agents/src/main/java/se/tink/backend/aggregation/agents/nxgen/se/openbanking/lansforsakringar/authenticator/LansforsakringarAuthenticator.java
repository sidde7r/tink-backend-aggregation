package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class LansforsakringarAuthenticator implements Authenticator {
    private final LansforsakringarApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public LansforsakringarAuthenticator(
            LansforsakringarApiClient apiClient,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        final AuthenticateForm form =
                AuthenticateForm.builder()
                        .setClientId(persistentStorage.get(StorageKeys.CLIENT_ID))
                        .setClientSecret(persistentStorage.get(StorageKeys.CLIENT_SECRET))
                        .setGrantType(FormValues.CLIENT_CREDENTIALS)
                        .build();

        final OAuth2Token token = apiClient.authenticate(form);

        sessionStorage.put(StorageKeys.ACCESS_TOKEN, token);
    }
}
