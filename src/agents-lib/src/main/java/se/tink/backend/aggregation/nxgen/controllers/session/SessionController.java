package se.tink.backend.aggregation.nxgen.controllers.session;

import com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.SerializeContainer;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SessionController {

    private final AgentContext context;
    private final TinkHttpClient httpClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final SessionHandler sessionHandler;
    private final Credentials credentials;

    public SessionController(AgentContext context, TinkHttpClient httpClient, PersistentStorage persistentStorage,
            SessionStorage sessionStorage, Credentials credentials, SessionHandler sessionHandler) {
        Preconditions.checkNotNull(sessionHandler, "What are you doing handling sessions without a "
                + "session handler?");
        this.context = context;
        this.httpClient = httpClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.sessionHandler = sessionHandler;
        this.credentials = credentials;
    }

    public void logout() {
        try {
            sessionHandler.logout();
        } finally {
            clear();
        }
    }

    public boolean isLoggedIn() {
        try {
            sessionHandler.keepAlive();
            return true;
        } catch (SessionException e) {
            Preconditions.checkState(Objects.equals(e.getError(), SessionError.SESSION_EXPIRED), e);
            return false;
        }
    }

    public void store() {
        // Store http client
        credentials.setSensitivePayload(Field.Key.HTTP_CLIENT, httpClient.serialize());

        // Store custom session values
        credentials.setSensitivePayload(Field.Key.SESSION_STORAGE,
                SerializationUtils.serializeToString(sessionStorage));

        credentials.setSensitivePayload(Field.Key.PERSISTENT_STORAGE,
                SerializationUtils.serializeToString(persistentStorage));
    }

    public void load() {
        // Load http client
        credentials.getSensitivePayload(Field.Key.HTTP_CLIENT, SerializeContainer.class).ifPresent(httpClient::initialize);

        // Load session
        // Note: We deserialize only the map and not the whole `SessionStorage` object in order
        // to not overwrite the reference as it's potentially used in agents.
        credentials.getSensitivePayload(Field.Key.SESSION_STORAGE, SessionStorage.class)
                .ifPresent(sessionStorage::putAll);

        credentials.getSensitivePayload(Field.Key.PERSISTENT_STORAGE, PersistentStorage.class)
                .ifPresent(persistentStorage::putAll);
    }

    public void clear() {
        // Clear http client
        credentials.removeSensitivePayload(Field.Key.HTTP_CLIENT);
        httpClient.clearCookies();
        httpClient.clearPersistentHeaders();

        // Clear session
        credentials.removeSensitivePayload(Field.Key.SESSION_STORAGE);
        sessionStorage.clear();
    }
}
