package se.tink.backend.main.auth;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.auth.AuthenticationContext;
import se.tink.backend.auth.AuthenticationContextRequest;
import se.tink.backend.auth.AuthenticationDetails;
import se.tink.backend.common.repository.mysql.main.OAuth2ClientRepository;
import se.tink.backend.core.Client;
import se.tink.backend.core.User;
import se.tink.backend.core.oauth2.OAuth2Client;
import se.tink.backend.main.auth.loaders.OAuth2ClientFromDatabaseSupplier;
import se.tink.backend.main.providers.ClientProvider;
import se.tink.libraries.auth.HttpAuthenticationMethod;

public class DefaultAuthenticationContext implements AuthenticationContext {

    private final AuthenticationDetails authenticationDetails;
    private final Client client;
    private final Supplier<Optional<OAuth2Client>> oauth2ClientSupplier;
    private final String remoteAddress;
    private final User user;
    private final String userAgent;
    private final String userDeviceId;
    private boolean administrativeMode;
    private Map<String, String> metadata;

    public DefaultAuthenticationContext(
            AuthenticationDetails authenticationDetails,
            Client client,
            Supplier<Optional<OAuth2Client>> oauth2ClientSupplier,
            String remoteAddress,
            User user,
            String userAgent,
            String userDeviceId,
            boolean administrativeMode,
            Map<String, String> metadata) {

        this.authenticationDetails = authenticationDetails;
        this.client = client;
        this.oauth2ClientSupplier = oauth2ClientSupplier;
        this.remoteAddress = remoteAddress;
        this.user = user;
        this.userAgent = userAgent;
        this.userDeviceId = userDeviceId;
        this.administrativeMode = administrativeMode;
        this.metadata = metadata;
    }

    @Override
    public Optional<OAuth2Client> getOAuth2Client() {
        return oauth2ClientSupplier.get();
    }

    public Optional<String> getOAuth2ClientId() {
        Optional<OAuth2Client> oAuth2Client = getOAuth2Client();
        if (!oAuth2Client.isPresent()) {
            return Optional.empty();
        }
        return Optional.ofNullable(oAuth2Client.get().getId());
    }

    @Override
    public Optional<Client> getClient() {
        return Optional.ofNullable(client);
    }

    @Override
    public Optional<String> getUserAgent() {
        return Optional.ofNullable(userAgent);
    }

    @Override
    public Optional<String> getRemoteAddress() {
        return Optional.ofNullable(remoteAddress);
    }

    @Override
    public Optional<String> getUserDeviceId() {
        return Optional.ofNullable(userDeviceId);
    }

    @Override
    public boolean isAuthenticated() {
        return user != null;
    }

    @Override
    public boolean isAdministrativeMode() {
        return administrativeMode;
    }

    @Override
    public User getUser() {
        Preconditions.checkArgument(isAuthenticated(), "Caller should verify if authenticated before");
        return user;
    }

    @Override
    public HttpAuthenticationMethod getHttpAuthenticationMethod() {
        Preconditions.checkArgument(isAuthenticated(), "Caller should verify if authenticated before");
        return authenticationDetails.getMethod();
    }

    @Override
    public Map<String, String> getMetadata() {
        return metadata;
    }

    public AuthenticationDetails getAuthenticationDetails() {
        return authenticationDetails;
    }

    public static class Builder {

        private final OAuth2ClientRepository oauth2ClientRepository;
        private final ClientProvider clientProvider;

        private final String remoteAddress;
        private final String userAgent;
        private final AuthenticationDetails authenticationDetails;

        private String clientKey;
        private Client client;
        private OAuth2Client oauth2Client;
        private String oauth2ClientId;
        private String userDeviceId;
        private User user;
        private boolean administrativeMode;
        private Map<String, String> headers;

        public Builder(OAuth2ClientRepository oauth2ClientRepository,
                ClientProvider clientProvider, AuthenticationContextRequest context) {
            this.oauth2ClientRepository = oauth2ClientRepository;
            this.clientProvider = clientProvider;

            this.clientKey = context.getClientKey().orElse(null);
            this.oauth2ClientId = context.getOauth2ClientId().orElse(null);
            this.authenticationDetails = context.getAuthenticationDetails().orElse(null);
            this.remoteAddress = context.getRemoteAddress().orElse(null);
            this.userDeviceId = context.getUserDeviceId().orElse(null);
            this.userAgent = context.getUserAgent().orElse(null);

            this.administrativeMode = false; // explicit
            this.headers = context.getClientKey() == null ? Maps.newHashMap() : context.getHeaders();

        }

        public Builder setClient(Client client) {
            this.client = client;
            return this;
        }

        public Builder setOAuth2Client(OAuth2Client oauth2Client) {
            this.oauth2Client = oauth2Client;
            return this;
        }

        public Builder setOAuth2ClientId(String oauth2ClientId) {
            this.oauth2ClientId = oauth2ClientId;
            return this;
        }

        public Builder setAdministrativeMode(boolean administrativeMode) {
            this.administrativeMode = administrativeMode;
            return this;
        }

        public Builder setUser(User user) {
            this.user = user;
            return this;
        }

        public Builder setAuthenticatedUser(AuthenticatedUser authenticatedUser) {
            if (authenticatedUser != null) {
                setUser(authenticatedUser.getUser());
                setOAuth2ClientId(authenticatedUser.getOAuthClientId());
                setAdministrativeMode(authenticatedUser.isAdministrativeMode());
            }

            return this;
        }

        public DefaultAuthenticationContext build() {

            return new DefaultAuthenticationContext(
                    authenticationDetails,
                    getOrProvideClient(),
                    getOAuth2ClientSupplier(oauth2ClientRepository),
                    remoteAddress,
                    user,
                    userAgent,
                    userDeviceId,
                    administrativeMode,
                    headers);
        }

        private Supplier<Optional<OAuth2Client>> getOAuth2ClientSupplier(
                OAuth2ClientRepository oauth2ClientRepository) {
            if (oauth2Client != null) {
                return Suppliers.ofInstance(Optional.of(oauth2Client));
            } else if (oauth2ClientId == null) {
                return Suppliers.ofInstance(Optional.empty());
            } else {
                return Suppliers.memoize(new OAuth2ClientFromDatabaseSupplier(oauth2ClientRepository, oauth2ClientId));
            }
        }

        private Client getOrProvideClient() {
            if (client != null) {
                return client;
            }

            if (Strings.isNullOrEmpty(clientKey)) {
                return null;
            }
            return clientProvider.get().get(clientKey);
        }
    }
}
