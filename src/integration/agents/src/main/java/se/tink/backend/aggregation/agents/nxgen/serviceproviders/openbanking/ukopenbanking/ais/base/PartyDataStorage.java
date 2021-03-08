package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PartyDataStorage {

    private final PersistentStorage persistentStorage;
    public static final String RECENT_PARTY_DATA = "recent_identity_data";
    public static final String RECENT_PARTY_DATA_LIST = "recent_identity_data_list";

    public PartyDataStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public Optional<PartyV31Entity> restoreParty() {
        return persistentStorage.get(RECENT_PARTY_DATA, PartyV31Entity.class);
    }

    public void storeParty(PartyV31Entity data) {
        persistentStorage.put(RECENT_PARTY_DATA, data);
    }

    public List<PartyV31Entity> restoreParties() {
        return persistentStorage
                .get(RECENT_PARTY_DATA_LIST, new TypeReference<List<PartyV31Entity>>() {})
                .orElseGet(Collections::emptyList);
    }

    public void storeParties(List<PartyV31Entity> data) {
        persistentStorage.put(RECENT_PARTY_DATA_LIST, data);
    }
}
