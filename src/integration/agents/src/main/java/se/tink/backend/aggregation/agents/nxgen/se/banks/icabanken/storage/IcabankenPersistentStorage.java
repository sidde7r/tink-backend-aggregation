package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IcabankenPersistentStorage {
    private final PersistentStorage persistentStorage;

    public IcabankenPersistentStorage(
            PersistentStorage persistentStorage, RandomValueGenerator randomValueGenerator) {
        this.persistentStorage = persistentStorage;

        if (persistentStorage.containsKey(IcaBankenConstants.IdTags.DEVICE_APPLICATION_ID)) {
            // this is no longer used in the API
            persistentStorage.remove(IcaBankenConstants.IdTags.DEVICE_APPLICATION_ID);
        }

        if (!persistentStorage.containsKey(IcaBankenConstants.IdTags.USER_INSTALLATION_ID)) {
            // generate installation ID
            persistentStorage.put(
                    IcaBankenConstants.IdTags.USER_INSTALLATION_ID,
                    randomValueGenerator.getUUID().toString());
        }
    }

    public String getUserInstallationId() {
        return persistentStorage.get(IcaBankenConstants.IdTags.USER_INSTALLATION_ID);
    }
}
