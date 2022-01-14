package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class FetchedTransactionsDataStorageTest {
    private FetchedTransactionsDataStorage fetchedTransactionsDataStorage;
    private PersistentStorage persistentStorage;

    @Before
    public void setUp() throws Exception {
        persistentStorage = new PersistentStorage();
        fetchedTransactionsDataStorage = new FetchedTransactionsDataStorage(persistentStorage);
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenPersistentStorageIsNull() {
        assertThatThrownBy(() -> new FetchedTransactionsDataStorage(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Persistent storage can not be null!");
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenSettingFetchedTransactionsToNullAccountId() {
        LocalDateTime sampleDateTime = getSampleLocalDateTime();
        assertThatThrownBy(
                        () ->
                                fetchedTransactionsDataStorage.setFetchedTransactionsUntil(
                                        null, sampleDateTime))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Account ID can not be null!");
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenSettingFetchedTransactionsToNullDateTime() {
        assertThatThrownBy(
                        () ->
                                fetchedTransactionsDataStorage.setFetchedTransactionsUntil(
                                        "dummyID", null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("Request dateTime can not be null!");
    }

    @Test
    public void shouldStoreFetchedTransactionsWithProperAccountId() {
        // given
        LocalDateTime sampleLocalDateTime = getSampleLocalDateTime();
        String accountId = "dummyID";

        // when
        fetchedTransactionsDataStorage.setFetchedTransactionsUntil(accountId, sampleLocalDateTime);

        // then
        assertThat(persistentStorage).hasSize(1);
        assertThat(persistentStorage.keySet().stream().anyMatch(k -> k.contains("dummyID")))
                .isTrue();
        assertThat(persistentStorage.values().stream().map(LocalDateTime::parse))
                .contains(sampleLocalDateTime);
    }

    @Test
    public void shouldThrowNullPointerExceptionWhenGettingFetchedTransactionsWithNullAccountId() {
        assertThatThrownBy(() -> fetchedTransactionsDataStorage.getFetchedTransactionsUntil(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Account ID can not be null!");
    }

    @Test
    public void shouldGetFetchedTransactionsWhenProperAccountIdIsApplied() {
        // given
        LocalDateTime sampleLocalDateTime = getSampleLocalDateTime();
        fetchedTransactionsDataStorage.setFetchedTransactionsUntil("dummyId", sampleLocalDateTime);

        // when
        Optional<LocalDateTime> requestTime =
                fetchedTransactionsDataStorage.getFetchedTransactionsUntil("dummyId");
        Optional<LocalDateTime> emptyRequestTime =
                fetchedTransactionsDataStorage.getFetchedTransactionsUntil("anotherDummyId");

        // then
        assertThat(requestTime).isNotEmpty().contains(getSampleLocalDateTime());
        assertThat(emptyRequestTime).isEmpty();
    }

    private LocalDateTime getSampleLocalDateTime() {
        return LocalDateTime.of(2020, 3, 1, 10, 15);
    }
}
