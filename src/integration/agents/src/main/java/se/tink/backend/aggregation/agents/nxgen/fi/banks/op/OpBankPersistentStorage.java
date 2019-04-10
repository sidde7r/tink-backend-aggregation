package se.tink.backend.aggregation.agents.nxgen.fi.banks.op;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class OpBankPersistentStorage {
    private final Credentials credentials;
    private final PersistentStorage persistentStorage;

    public OpBankPersistentStorage(Credentials credentials, PersistentStorage persistentStorage) {
        this.credentials = credentials;
        this.persistentStorage = persistentStorage;
    }

    public String retrieveInstanceId() {
        if (persistentStorage.containsKey(OpBankConstants.Authentication.APPLICATION_INSTANCE_ID)) {
            return persistentStorage.get(OpBankConstants.Authentication.APPLICATION_INSTANCE_ID);
        }
        return se.tink.libraries.strings.StringUtils.hashAsUUID("TINK-" + credentials.getUserId())
                .toLowerCase();
    }

    public void put(String key, String value) {
        persistentStorage.put(key, value);
    }

    public boolean containsAppId() {
        return persistentStorage.containsKey(
                OpBankConstants.Authentication.APPLICATION_INSTANCE_ID);
    }
}
