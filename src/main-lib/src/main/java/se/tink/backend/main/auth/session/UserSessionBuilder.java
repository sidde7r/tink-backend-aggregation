package se.tink.backend.main.auth.session;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.backend.common.providers.OAuth2ClientProvider;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.Client;
import se.tink.backend.core.SessionTypes;
import se.tink.backend.core.User;
import se.tink.backend.core.UserSession;
import se.tink.backend.main.auth.exceptions.UnsupportedClientException;
import se.tink.backend.main.auth.exceptions.jersey.UnexistingOAuth2ClientJerseyException;
import se.tink.backend.main.auth.validators.ClientValidator;
import se.tink.backend.utils.StringUtils;

public class UserSessionBuilder {

    private static final LogUtils log = new LogUtils(UserSessionBuilder.class);

    private final OAuth2ClientProvider oauth2ClientProvider;
    private final ClientValidator clientValidator;
    private final User user;

    private Optional<String> clientKey = Optional.empty();
    private Optional<String> oauth2ClientId = Optional.empty();

    UserSessionBuilder(final OAuth2ClientProvider oauth2ClientProvider, final ClientValidator clientValidator,
            final User user) {

        this.oauth2ClientProvider = oauth2ClientProvider;
        this.clientValidator = clientValidator;
        this.user = user;
    }

    public UserSessionBuilder setClientKey(@Nullable String clientKey) {
        this.clientKey = Optional.ofNullable(clientKey);
        return this;
    }

    public UserSessionBuilder setOAuth2ClientId(@Nullable String oauth2ClientId) {
        this.oauth2ClientId = Optional.ofNullable(oauth2ClientId);
        return this;
    }

    public UserSession build() throws UnsupportedClientException, UnexistingOAuth2ClientJerseyException {
        UserSession session = new UserSession();

        session.setId(StringUtils.generateUUID());
        session.setUserId(user.getId());

        // the legacy fallback
        session.setSessionType(SessionTypes.WEB);

        if (clientKey.isPresent()) {
            Optional<Client> client = clientValidator.validateClient(clientKey.get(), user.getLocale());
            if (client.isPresent()) {
                session.setSessionType(client.get().getSessionType());

                if (oauth2ClientId.isPresent()) {
                    if (!oauth2ClientProvider.get().containsKey(oauth2ClientId.get())) {
                        log.warn(user.getId(), String.format("OAuth client id %s doesn't exist", oauth2ClientId.get()));
                        throw new UnexistingOAuth2ClientJerseyException();
                    }

                    session.setOAuthClientId(oauth2ClientId.get());
                }
            }
        }

        return session;
    }
}
