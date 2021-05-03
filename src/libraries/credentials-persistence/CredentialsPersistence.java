package se.tink.backend.aggregation.nxgen.controllers.session;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.serializecontainer.SerializeContainer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public final class CredentialsPersistence {

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final Credentials credentials;
    @Nullable private final TinkHttpClient httpClient;

    public CredentialsPersistence(
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            Credentials credentials,
            @Nullable TinkHttpClient httpClient) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.credentials = credentials;
        this.httpClient = httpClient;
    }

    public void store() {
        if (httpClient != null) {
            // Store http client
            credentials.setSensitivePayload(Field.Key.HTTP_CLIENT, httpClient.serialize());
        }

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
        if (httpClient != null) {
            credentials
                    .getSensitivePayload(Field.Key.HTTP_CLIENT, SerializeContainer.class)
                    .ifPresent(httpClient::initialize);
        }
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

    private Optional<Integer> getMapSizeInBytes(Map<String, String> map) {
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
        if (httpClient != null) {
            httpClient.clearCookies();
            httpClient.clearPersistentHeaders();
        }

        // Clear session
        credentials.removeSensitivePayload(Field.Key.SESSION_STORAGE);
        sessionStorage.clear();
    }
}
