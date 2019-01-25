package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaApiClient;
import se.tink.backend.aggregation.agents.utils.authentication.encap.EncapClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class AktiaAutoAuthenticator implements AutoAuthenticator {
    private final EncapClient encapClient;
    private final AktiaApiClient apiClient;
    private final Credentials credentials;

    public AktiaAutoAuthenticator(EncapClient encapClient, AktiaApiClient apiClient, Credentials credentials) {
        this.encapClient = encapClient;
        this.apiClient = apiClient;
        this.credentials = credentials;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        try {
            String securityToken = encapClient.authenticateUser();
            apiClient.authenticate(credentials.getField(Field.Key.USERNAME), securityToken);
        } catch (HttpResponseException e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
