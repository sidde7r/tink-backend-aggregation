package se.tink.backend.connector.util.handler;

import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.User;

public interface CredentialsHandler {

    Credentials findCredentials(User user, String externalUserId) throws RequestException;

    Credentials findCredentials(User user, String externalUserId, String providerName) throws RequestException;

    Credentials createCredentials(User user) throws RequestException;

    void storeCredentials(Credentials credentials);
}
