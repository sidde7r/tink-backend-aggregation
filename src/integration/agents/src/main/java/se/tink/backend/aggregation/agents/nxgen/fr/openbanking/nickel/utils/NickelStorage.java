package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.utils;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;

@RequiredArgsConstructor
public class NickelStorage {
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;

    public String getSessionData(String key) {
        return sessionStorage.get(key);
    }

    public String getPersistentData(String key) {
        return persistentStorage.get(key);
    }

    public String getOrCreateSessionData(String key, boolean dashed) {
        return getOrCreate(sessionStorage, key, dashed);
    }

    public String getOrCreatePersistentData(String key, boolean dashed) {
        return getOrCreate(persistentStorage, key, dashed);
    }

    public Object getSessionObject(String key) {
        return sessionStorage.get(key);
    }

    public Object getPersistentObject(String key) {
        return persistentStorage.get(key);
    }

    public void setSessionData(String key, String value) {
        sessionStorage.put(key, value);
    }

    public void setSessionObject(String key, Object value) {
        sessionStorage.put(key, value);
    }

    public void setPersistentData(String key, String value) {
        persistentStorage.put(key, value);
    }

    public void setPersistentObject(String key, Object value) {
        persistentStorage.put(key, value);
    }

    private String getOrCreate(Storage storage, String key, boolean dashed) {
        String value = storage.get(key);
        if (null == value) {
            value = String.valueOf(UUID.randomUUID());
            if (!dashed) value = value.replace("-", "");
            storage.put(key, value);
        }
        return value;
    }
}
