package se.tink.backend.connector.util.handler;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.common.config.ConnectorConfiguration;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.connector.exception.RequestException;
import se.tink.backend.connector.exception.error.RequestError;
import se.tink.backend.connector.util.helper.RepositoryHelper;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.User;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.system.rpc.UpdateCredentialsStatusRequest;

public class DefaultCredentialsHandler implements CredentialsHandler {

    private final ConnectorConfiguration connectorConfiguration;
    private RepositoryHelper repositoryHelper;
    private SystemServiceFactory systemServiceFactory;

    @Inject
    public DefaultCredentialsHandler(RepositoryHelper repositoryHelper, SystemServiceFactory systemServiceFactory,
            ServiceConfiguration serviceConfiguration) {
        this.repositoryHelper = repositoryHelper;
        this.systemServiceFactory = systemServiceFactory;
        this.connectorConfiguration = serviceConfiguration.getConnector();
    }

    @Override
    public Credentials findCredentials(User user, String externalUserId) throws RequestException {
        return findCredentials(user, externalUserId, connectorConfiguration.getDefaultProviderName());
    }

    @Override
    public Credentials findCredentials(User user, String externalUserId, String providerName) throws RequestException {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(connectorConfiguration.getDefaultProviderName()));

        List<Credentials> credentials = repositoryHelper.getCredentials(user, providerName);

        if (credentials.isEmpty()) {
            throw RequestError.CREDENTIALS_NOT_FOUND.exception().withExternalUserId(externalUserId);
        } else if (credentials.size() > 1) {
            throw RequestError.CREDENTIALS_MORE_THAN_1.exception().withExternalUserId(externalUserId);
        }

        return credentials.get(0);
    }

    @Override
    public Credentials createCredentials(User user) throws RequestException {
        Credentials credentials = new Credentials();

        credentials.setUserId(user.getId());
        credentials.setStatus(CredentialsStatus.CREATED);
        credentials.setType(CredentialsTypes.PASSWORD);

        Preconditions.checkArgument(!Strings.isNullOrEmpty(connectorConfiguration.getDefaultProviderName()));
        credentials.setProviderName(connectorConfiguration.getDefaultProviderName());

        return credentials;
    }

    @Override
    public void storeCredentials(Credentials credentials) {
        repositoryHelper.saveCredentials(credentials);
    }
}
