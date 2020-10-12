package se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.FetcherTestData;
import se.tink.backend.aggregation.agents.nxgen.it.bancoposta.fetcher.FetcherTestHelper;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Urls.IdentityUrl;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.identity.BancoPostaIdentityFetcher;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

public class BancoPostaIdentityFetcherTest {

    @Before
    public void init() {}

    @Test
    public void shouldFetchIdentityIfAvailable() {
        // given
        TinkHttpClient httpClient = mock(TinkHttpClient.class, Mockito.RETURNS_DEEP_STUBS);
        PersistentStorage persistentStorage = FetcherTestHelper.prepareMockedPersistenStorage();
        BancoPostaStorage storage = new BancoPostaStorage(persistentStorage);
        BancoPostaApiClient apiClient = new BancoPostaApiClient(httpClient, storage);
        BancoPostaIdentityFetcher fetcher = new BancoPostaIdentityFetcher(apiClient);

        // when
        RequestBuilder fetchIdentityMockRequestBuilder =
                FetcherTestHelper.mockRequestBuilder(IdentityUrl.FETCH_IDENTITY_DATA, httpClient);
        when(fetchIdentityMockRequestBuilder.post(any()))
                .thenReturn(FetcherTestData.getIdentityFetcherResponse());

        IdentityData identityData = fetcher.fetchIdentityData();

        // then
        assertThat(identityData.getFullName()).isEqualTo("dummyName dummyLastName");
        assertThat(identityData.getDateOfBirth()).isEqualTo(LocalDate.of(2000, 1, 1));
        assertThat(identityData.getSsn()).isEqualTo("dummySSN");
    }
}
