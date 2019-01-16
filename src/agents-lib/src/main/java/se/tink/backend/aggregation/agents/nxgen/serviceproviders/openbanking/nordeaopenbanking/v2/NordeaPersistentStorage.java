package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2;

import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class NordeaPersistentStorage {
    private final PersistentStorage persistentStorage;

    public NordeaPersistentStorage(PersistentStorage persistentStorage) {

        this.persistentStorage = persistentStorage;
    }

    public String getClientId() {
        return persistentStorage.get(NordeaBaseConstants.Storage.CLIENT_ID);
    }
    public void setClientId(String  clientId) {
        persistentStorage.put(NordeaBaseConstants.Storage.CLIENT_ID, clientId);
    }

    public String getClientsecret() {
        return persistentStorage.get(NordeaBaseConstants.Storage.CLIENT_SECRET);
    }
    public void setClientSecret(String  clientSecret) {
        persistentStorage.put(NordeaBaseConstants.Storage.CLIENT_SECRET, clientSecret);
    }

    public String getRedirectUrl() {
        return persistentStorage.get(NordeaBaseConstants.Storage.REDIRECT_URL);
    }

    public void setRedirectUrl(String redirectUrl) {
        persistentStorage.put(NordeaBaseConstants.Storage.REDIRECT_URL, redirectUrl);
    }
}
