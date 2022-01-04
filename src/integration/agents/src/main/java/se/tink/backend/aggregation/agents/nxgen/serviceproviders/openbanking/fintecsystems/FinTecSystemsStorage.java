package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class FinTecSystemsStorage {

    private static final String TRANSACTION_ID = "TRANSACTION_ID";

    private final PersistentStorage persistentStorage;

    public void storeTransactionId(String transactionId) {
        persistentStorage.put(TRANSACTION_ID, transactionId);
    }

    public Optional<String> retrieveTransactionId() {
        return persistentStorage.get(TRANSACTION_ID, String.class);
    }
}
