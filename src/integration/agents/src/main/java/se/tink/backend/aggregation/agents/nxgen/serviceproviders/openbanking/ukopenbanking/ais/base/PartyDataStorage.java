package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class PartyDataStorage {

    private final PersistentStorage persistentStorage;
    public static final String RECENT_PARTY_DATA = "recent_identity_data";
    public static final String RECENT_PARTY_DATA_LIST = "recent_identity_data_list";

    public PartyDataStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public Optional<PartyV31Entity> restoreParty() {
        Optional<PartyV31Entity> party =
                persistentStorage.get(RECENT_PARTY_DATA, PartyV31Entity.class);
        if (!party.isPresent()) {
            log.info("[RESTORE PARTY] Party has been never saved. Restoring not possible.");
            log.info(
                    "[RESTORE PARTY] Party data will not be available for this credentials until next full authentication");
        }
        return party;
    }

    public void storeParty(PartyV31Entity data) {
        persistentStorage.put(RECENT_PARTY_DATA, data);
    }

    public List<PartyV31Entity> restoreParties() {
        return persistentStorage
                .get(RECENT_PARTY_DATA_LIST, new TypeReference<List<PartyV31Entity>>() {})
                .orElseGet(this::emptyList);
    }

    public void storeParties(List<PartyV31Entity> data) {
        persistentStorage.put(RECENT_PARTY_DATA_LIST, data);
    }

    private List<PartyV31Entity> emptyList() {
        log.info("[RESTORE PARTIES] Parties has been never saved. Restoring not possible.");
        log.info(
                "[RESTORE PARTIES] Parties data will not be available for this credentials until next full authentication");
        return Collections.emptyList();
    }
}
