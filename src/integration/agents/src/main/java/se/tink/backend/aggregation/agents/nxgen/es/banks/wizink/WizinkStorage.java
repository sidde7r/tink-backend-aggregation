package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.entities.ProductEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@Slf4j
public class WizinkStorage {
    private static final String DEVICE_ID = "deviceId";
    private static final String INDIGITALL_DEVICE = "indigitallDevice";

    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public WizinkStorage(
            final PersistentStorage persistentStorage, final SessionStorage sessionStorage) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    public void storeDeviceId(String deviceId) {
        persistentStorage.put(DEVICE_ID, deviceId);
    }

    public String getDeviceId() {
        return persistentStorage.get(DEVICE_ID);
    }

    public void storeIndigitallDevice(String indigitallDevice) {
        sessionStorage.put(INDIGITALL_DEVICE, indigitallDevice);
    }

    public String getIndigitallDevice() {
        return sessionStorage.get(INDIGITALL_DEVICE);
    }

    public void storeCreditCardData(List<CardEntity> creditcards) {
        persistentStorage.put(StorageKeys.CARDS_LIST, creditcards);
    }

    public void storeAccounts(List<ProductEntity> accounts) {
        persistentStorage.put(StorageKeys.ACCOUNTS_LIST, accounts);
    }

    public List<CardEntity> getCreditCardList() {
        return persistentStorage
                .get(StorageKeys.CARDS_LIST, new TypeReference<List<CardEntity>>() {})
                .orElseGet(
                        () -> {
                            log.info("No cards found");
                            return Collections.emptyList();
                        });
    }

    public List<ProductEntity> getAccounts() {
        return persistentStorage
                .get(StorageKeys.ACCOUNTS_LIST, new TypeReference<List<ProductEntity>>() {})
                .orElseGet(
                        () -> {
                            log.info("No accounts found");
                            return Collections.emptyList();
                        });
    }

    public void storeXTokenId(String token) {
        sessionStorage.put(StorageKeys.X_TOKEN_ID, token);
    }

    public String getXTokenId() {
        return sessionStorage.get(StorageKeys.X_TOKEN_ID);
    }

    public void storeXTokenUser(String token) {
        sessionStorage.put(StorageKeys.X_TOKEN_USER, token);
    }

    public String getXTokenUser() {
        return sessionStorage.get(StorageKeys.X_TOKEN_USER);
    }

    public void markIsNotFirstFullRefresh() {
        persistentStorage.put(StorageKeys.FIRST_FULL_REFRESH, false);
    }

    public void markIsFirstFullRefresh() {
        persistentStorage.put(StorageKeys.FIRST_FULL_REFRESH, true);
    }

    public boolean getFirstFullRefreshFlag() {
        return persistentStorage.get(StorageKeys.FIRST_FULL_REFRESH, Boolean.class).orElse(false);
    }
}
