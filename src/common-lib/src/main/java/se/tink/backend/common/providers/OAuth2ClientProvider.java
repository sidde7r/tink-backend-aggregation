package se.tink.backend.common.providers;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.concurrent.TimeUnit;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.core.oauth2.OAuth2Client;

public class OAuth2ClientProvider implements Provider<ImmutableMap<String, OAuth2Client>> {
    private final Supplier<ImmutableMap<String, OAuth2Client>> supplier;

    @Inject
    public OAuth2ClientProvider(final OAuth2ClientRepository repository) {
        this.supplier = Suppliers.memoizeWithExpiration(() -> FluentIterable.from(repository.findAll())
                .uniqueIndex(OAuth2Client::getId), 10, TimeUnit.MINUTES);
    }

    @Override
    public ImmutableMap<String, OAuth2Client> get() {
        return supplier.get();
    }
}
