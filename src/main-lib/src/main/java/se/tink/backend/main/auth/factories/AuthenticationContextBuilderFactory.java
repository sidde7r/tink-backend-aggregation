package se.tink.backend.main.auth.factories;

import com.google.inject.Inject;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.main.auth.DefaultAuthenticationContext;
import se.tink.backend.main.providers.ClientProvider;

public class AuthenticationContextBuilderFactory {
    private final OAuth2ClientRepository clientRepository;
    private final ClientProvider clientProvider;

    @Inject
    public AuthenticationContextBuilderFactory(final OAuth2ClientRepository clientRepository,
            ClientProvider clientProvider) {
        this.clientRepository = clientRepository;
        this.clientProvider = clientProvider;
    }

    public DefaultAuthenticationContext.Builder create(AuthenticationContextRequest context) {
        return new DefaultAuthenticationContext.Builder(clientRepository, clientProvider, context);
    }
}
