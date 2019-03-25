package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.rpc.AuthenticateForm;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

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
        AuthenticateForm form =
                AuthenticateForm.builder()
                        .setClientId(
                                persistentStorage.get(
                                        LansforsakringarConstants.StorageKeys.CLIENT_ID))
                        .setClientSecret(
                                persistentStorage.get(
                                        LansforsakringarConstants.StorageKeys.CLIENT_SECRET))
                        .setGrantType(LansforsakringarConstants.FormValues.CLIENT_CREDENTIALS)
                        .build();

        OAuth2Token token = apiClient.authenticate(form);
        sessionStorage.put(LansforsakringarConstants.StorageKeys.ACCESS_TOKEN, token);
    }
}
