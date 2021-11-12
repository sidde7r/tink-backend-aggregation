package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator.LaBanquePostaleAccountSegment;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LaBanquePostaleUserState {

    private static final String ACCOUNT_SEGMENT = "ACCOUNT_SEGMENT";
    private final PersistentStorage persistentStorage;

    public LaBanquePostaleUserState(final PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public void specifyAccountSegment(LaBanquePostaleAccountSegment accountSegment) {
        persistentStorage.put(ACCOUNT_SEGMENT, accountSegment);
    }

    public boolean isBusinessAccountSegment() {
        return persistentStorage
                .get(ACCOUNT_SEGMENT, LaBanquePostaleAccountSegment.class)
                .map(LaBanquePostaleAccountSegment.BUSINESS::equals)
                .orElse(false);
    }

    public boolean isAccountSegmentSpecified() {
        return persistentStorage.containsKey(ACCOUNT_SEGMENT);
    }
}
