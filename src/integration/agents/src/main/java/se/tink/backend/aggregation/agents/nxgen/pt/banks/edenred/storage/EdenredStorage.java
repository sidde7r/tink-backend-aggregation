package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.TransactionsEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EdenredStorage {

    private PersistentStorage persistentStorage;
    private SessionStorage sessionStorage;

    public EdenredStorage(PersistentStorage persistentStorage, SessionStorage sessionStorage) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    public void setPin(String pin) {
        persistentStorage.put(Storage.PIN, pin);
    }

    public String getPin() {
        return persistentStorage.get(Storage.PIN);
    }

    public void setUserId(String userId) {
        persistentStorage.put(Storage.USER_ID, userId);
    }

    public String getUserId() {
        return persistentStorage.get(Storage.USER_ID);
    }

    public void setDeviceId(String deviceId) {
        persistentStorage.put(Storage.DEVICE_ID, deviceId);
    }

    public String getDeviceId() {
        return persistentStorage.get(Storage.DEVICE_ID);
    }

    public boolean isRegistered() {
        return persistentStorage.containsKey(Storage.USER_ID);
    }

    public void setToken(String token) {
        sessionStorage.put(Storage.TOKEN, token);
    }

    public String getToken() {
        return sessionStorage.get(Storage.TOKEN);
    }

    public void storeTransactions(Long accountId, TransactionsEntity data) {
        sessionStorage.put(Storage.TRANSACTIONS + accountId, data);
    }

    public Optional<TransactionsEntity> getTransactions(Long accountId) {
        return sessionStorage.get(Storage.TRANSACTIONS + accountId, TransactionsEntity.class);
    }

    public void cleanTransactions(Long accountId) {
        sessionStorage.remove(Storage.TRANSACTIONS + accountId);
    }
}
