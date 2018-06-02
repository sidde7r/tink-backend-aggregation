package se.tink.backend.main.auth.loaders;

import java.util.Optional;
import com.google.common.base.Supplier;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.core.oauth2.OAuth2Client;

public class OAuth2ClientFromDatabaseSupplier implements Supplier<Optional<OAuth2Client>> {
    private final OAuth2ClientRepository repository;
    private final String clientId;

    public OAuth2ClientFromDatabaseSupplier(OAuth2ClientRepository repository, String clientId) {
        this.repository = repository;
        this.clientId = clientId;
    }

    public Optional<OAuth2Client> get() {
        return Optional.ofNullable(repository.findOne(clientId));
    }
}
