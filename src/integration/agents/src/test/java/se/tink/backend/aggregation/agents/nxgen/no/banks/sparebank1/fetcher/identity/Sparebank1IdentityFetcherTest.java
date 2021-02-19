package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.exceptions.refresh.IdentityRefreshException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.identity.entities.IdentityDataEntity;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

@RunWith(JUnitParamsRunner.class)
public class Sparebank1IdentityFetcherTest {
    private Sparebank1identityFetcher identityFetcher;
    private PersistentStorage storage;
    private static final String FULL_NAME = "fullName";

    @Before
    public void init() {
        storage = mock(PersistentStorage.class);
        identityFetcher = new Sparebank1identityFetcher(storage);
    }

    @Test
    @Parameters(method = "generateIdentityDataEntites")
    public void fetchIdentityDataShouldReturnIdentityData(
            IdentityDataEntity identity, LocalDate matchingDate) {
        // given
        when(storage.get("identityData", IdentityDataEntity.class))
                .thenReturn(Optional.ofNullable(identity));

        // when
        IdentityData identityData = identityFetcher.fetchIdentityData();

        // then
        assertThat(identityData.getDateOfBirth()).isEqualTo(matchingDate);
        assertThat(identityData.getFullName()).isEqualTo(FULL_NAME);
    }

    private Object[] generateIdentityDataEntites() {
        return new Object[] {
            new Object[] {
                new IdentityDataEntity(FULL_NAME, "010593 *****"), LocalDate.of(1993, 05, 01)
            },
            new Object[] {
                new IdentityDataEntity(FULL_NAME, "310104 *****"), LocalDate.of(2004, 01, 31)
            },
            new Object[] {new IdentityDataEntity(FULL_NAME, "**********"), null}
        };
    }

    @Test
    public void
            fetchIdentityDataShouldThrowExceptionIfIdentityDataNotRetreivedFromPersistentStorage() {
        // given
        when(storage.get("identityData", IdentityDataEntity.class)).thenReturn(Optional.empty());
        // when
        Throwable throwable = catchThrowable(() -> identityFetcher.fetchIdentityData());
        // then
        assertThat(throwable).isInstanceOf(IdentityRefreshException.class);
    }
}
