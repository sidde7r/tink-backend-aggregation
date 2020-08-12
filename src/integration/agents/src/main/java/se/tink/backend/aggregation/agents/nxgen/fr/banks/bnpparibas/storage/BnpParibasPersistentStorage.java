package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.storage;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BnpParibasPersistentStorage {
    private final PersistentStorage persistentStorage;

    public BnpParibasPersistentStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public boolean isRegisteredDevice() {
        return persistentStorage.containsKey(BnpParibasConstants.Storage.LOGIN_ID);
    }

    public void saveLoginId(String loginId) {
        persistentStorage.put(BnpParibasConstants.Storage.LOGIN_ID, loginId);
    }

    public String getLoginId() {
        return persistentStorage.get(BnpParibasConstants.Storage.LOGIN_ID);
    }

    public void storeIdfaValue(String idfa) {
        persistentStorage.put(BnpParibasConstants.Storage.IDFA_UUID, idfa);
    }

    public void storeIdfvValue(String idfv) {
        persistentStorage.put(BnpParibasConstants.Storage.IDFV_UUID, idfv);
    }

    public String getIdfaValue() {
        return persistentStorage.get(BnpParibasConstants.Storage.IDFA_UUID);
    }

    public String getIdfVValue() {
        return persistentStorage.get(BnpParibasConstants.Storage.IDFV_UUID);
    }
}
