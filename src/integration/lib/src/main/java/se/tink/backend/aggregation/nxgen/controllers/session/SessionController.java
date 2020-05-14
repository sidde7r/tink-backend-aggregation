package se.tink.backend.aggregation.nxgen.controllers.session;

import com.google.common.base.Preconditions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.serializecontainer.SerializeContainer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public final class SessionController {

    private static Logger log = LoggerFactory.getLogger(SessionController.class);

    private final TinkHttpClient httpClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final SessionHandler sessionHandler;
    private final Credentials credentials;

    public SessionController(
            TinkHttpClient httpClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            Credentials credentials,
            SessionHandler sessionHandler) {
        Preconditions.checkNotNull(
                sessionHandler, "What are you doing handling sessions without a session handler?");
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
            log.info("SessionException in isLoggedIn: {}", e.getUserMessage().get());
            Preconditions.checkState(Objects.equals(e.getError(), SessionError.SESSION_EXPIRED), e);
            return false;
        }
    }

    public void store() {
        // Store http client
        credentials.setSensitivePayload(Field.Key.HTTP_CLIENT, httpClient.serialize());

        // Store custom session values
        credentials.setSensitivePayload(
                Field.Key.SESSION_STORAGE, SerializationUtils.serializeToString(sessionStorage));

        credentials.setSensitivePayload(
                Field.Key.PERSISTENT_STORAGE,
                SerializationUtils.serializeToString(persistentStorage));
    }

    public void load() {
        // Load http client
        long start = System.nanoTime();
        credentials
                .getSensitivePayload(Field.Key.HTTP_CLIENT, SerializeContainer.class)
                .ifPresent(httpClient::initialize);
        long stop = System.nanoTime();
        long total = stop - start;
        long initHttpClientTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);

        // Load session
        // Note: We deserialize only the map and not the whole `SessionStorage` object in order
        // to not overwrite the reference as it's potentially used in agents.
        start = System.nanoTime();
        credentials
                .getSensitivePayload(Field.Key.SESSION_STORAGE, SessionStorage.class)
                .ifPresent(sessionStorage::putAll);
        stop = System.nanoTime();
        total = stop - start;
        long loadSessionStorageTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);

        start = System.nanoTime();
        credentials
                .getSensitivePayload(Field.Key.PERSISTENT_STORAGE, PersistentStorage.class)
                .ifPresent(persistentStorage::putAll);
        stop = System.nanoTime();
        total = stop - start;
        long loadPersistentStorageTime = TimeUnit.SECONDS.convert(total, TimeUnit.NANOSECONDS);
        log.info(
                "Init http client: {}, Loading session storage: {}, Load  persistent storage: {}",
                initHttpClientTime,
                loadSessionStorageTime,
                loadPersistentStorageTime);
        Optional<Integer> sessionSize = getMapSizeInBytes(sessionStorage);
        sessionSize.ifPresent(size -> log.info("Size of sessionStorage {} bytes", size));
        Optional<Integer> persistenSize = getMapSizeInBytes(persistentStorage);
        persistenSize.ifPresent(size -> log.info("Size of persistentStorage {} bytes", size));
    }

    private Optional<Integer> getMapSizeInBytes(Map map) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(map);
            oos.close();
            return Optional.of(baos.size());
        } catch (IOException e) {
            return Optional.empty();
        }
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
