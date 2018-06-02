package se.tink.backend.main;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.repository.RepositoryFactory;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.core.oauth2.OAuth2Client;

public class Suppliers {

    private final Supplier<ImmutableMap<String, OAuth2Client>> oAuth2ClientsById;

    public Suppliers(final RepositoryFactory repositoryFactory) {
        final OAuth2ClientRepository oAuth2ClientRepository = repositoryFactory.getRepository(
                OAuth2ClientRepository.class);

        oAuth2ClientsById = createOAuth2ClientSupplier(oAuth2ClientRepository);
    }

    public Supplier<ImmutableMap<String, OAuth2Client>> getOAuth2ClientsById() {
        return oAuth2ClientsById;
    }



    private Supplier<ImmutableMap<String, OAuth2Client>> createOAuth2ClientSupplier(
            final OAuth2ClientRepository oAuth2ClientRepository) {

        return com.google.common.base.Suppliers.memoizeWithExpiration(() -> {
            List<OAuth2Client> clients = oAuth2ClientRepository.findAll();
            return Maps.uniqueIndex(clients, OAuth2Client::getId);
        }, 30, TimeUnit.MINUTES);
    }
}
