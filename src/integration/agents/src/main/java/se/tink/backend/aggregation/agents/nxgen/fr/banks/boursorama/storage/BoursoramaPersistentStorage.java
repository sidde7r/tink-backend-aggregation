package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.storage;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BoursoramaPersistentStorage {
    private final PersistentStorage persistentStorage;

    public BoursoramaPersistentStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public void saveUserHash(String userHash) {
        persistentStorage.put(BoursoramaConstants.Storage.USER_HASH, userHash);
    }

    public String getUserHash() {
        return persistentStorage.get(BoursoramaConstants.Storage.USER_HASH);
    }

    public void saveDeviceEnrolmentTokenValue(String deviceEnrolmentTokenValue) {
        persistentStorage.put(
                BoursoramaConstants.Storage.DEVICE_ENROLMENT_TOKEN_VALUE,
                deviceEnrolmentTokenValue);
    }

    public String getDeviceEnrolmentTokenValue() {
        return persistentStorage.get(BoursoramaConstants.Storage.DEVICE_ENROLMENT_TOKEN_VALUE);
    }

    public void saveUdid(String udid) {
        persistentStorage.put(BoursoramaConstants.Storage.UDID, udid);
    }

    public String getUdid() {
        return persistentStorage.get(BoursoramaConstants.Storage.UDID);
    }
}
