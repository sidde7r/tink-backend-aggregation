package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class FetchedTransactionsDataStorage {
    private static final String FETCHED_TRANSACTIONS_UNTIL = "fetchedTxUntil:";

    private final PersistentStorage persistentStorage;

    public FetchedTransactionsDataStorage(PersistentStorage persistentStorage) {
        this.persistentStorage =
                checkNotNull(persistentStorage, "Persistent storage can not be null!");
    }

    public void setFetchedTransactionsUntil(String accountId, LocalDateTime requestTime) {
        checkNotNull(accountId, "Account ID can not be null!");
        checkNotNull(requestTime, "Request dateTime can not be null!");
        final String fetchedUntilDate = requestTime.format(DateTimeFormatter.ISO_DATE_TIME);
        persistentStorage.put(FETCHED_TRANSACTIONS_UNTIL + accountId, fetchedUntilDate);
    }

    public Optional<LocalDateTime> getFetchedTransactionsUntil(String accountId) {
        checkNotNull(accountId, "Account ID can not be null!");
        String dateString = persistentStorage.get(FETCHED_TRANSACTIONS_UNTIL + accountId);
        return Optional.ofNullable(dateString)
                .map(date -> LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME));
    }
}
