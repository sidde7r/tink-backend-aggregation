package se.tink.backend.aggregation.agents.nxgen.es.banks.popular;

import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContracts;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BancoPopularPersistentStorage {

    private final PersistentStorage persistentStorage;

    public BancoPopularPersistentStorage(PersistentStorage persistentStorage) {

        this.persistentStorage = persistentStorage;
    }

    public BancoPopularContracts getLoginContracts() {
        return persistentStorage.get(BancoPopularConstants.PersistentStorage.LOGIN_CONTRACTS,
                BancoPopularContracts.class).orElse(new BancoPopularContracts());
    }

    public void setContracts(BancoPopularContracts contracts) {
        persistentStorage.put(BancoPopularConstants.PersistentStorage.LOGIN_CONTRACTS, contracts);
    }

    public String getIp() {
        return persistentStorage.get(BancoPopularConstants.PersistentStorage.CLIENT_IP);
    }

    public void setIp(String ip) {
        persistentStorage.put(BancoPopularConstants.PersistentStorage.CLIENT_IP, ip);
    }
}
