package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;

public class N26IdentityFetcherTest {

    private N26IdentityDataFetcher identityDataFetcher;

    @Before
    public void before() {
        // given
        N26ApiClient client = mock(N26ApiClient.class);
        when(client.fetchIdentityData())
                .thenReturn(N26IdentityDataFetcherTestData.fetchIdentityResponse());
        identityDataFetcher = new N26IdentityDataFetcher(client);
    }

    @Test
    public void shouldFetchIdentity() {
        // when
        FetchIdentityDataResponse fetchIdentityDataResponse =
                identityDataFetcher.fetchIdentityData();
        // then
        assertThat(fetchIdentityDataResponse).isNotNull();
        assertThat(fetchIdentityDataResponse.getIdentityData()).isNotNull();
        assertThat(fetchIdentityDataResponse.getIdentityData())
                .hasFieldOrPropertyWithValue("dateOfBirth", LocalDate.of(1971, 2, 2));
        assertThat(fetchIdentityDataResponse.getIdentityData().getFullName())
                .isEqualTo("Jan Janusz Zbigniew Maria Kowalski Nowak");
    }
}
