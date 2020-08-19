package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.PSU_NAME;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.LclTestFixtures.createEndUserIdentityResponseDto;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.identity.EndUserIdentityResponseDto;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.identity.LclIdentityFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class LclIdentityFetcherTest {

    private LclIdentityFetcher lclIdentityFetcher;

    @Before
    public void setUp() {
        final LclApiClient apiClientMock = mock(LclApiClient.class);
        final EndUserIdentityResponseDto endUserIdentityResponseDtoMock =
                createEndUserIdentityResponseDto();

        when(apiClientMock.getEndUserIdentity()).thenReturn(endUserIdentityResponseDtoMock);

        lclIdentityFetcher = new LclIdentityFetcher(apiClientMock);
    }

    @Test
    public void shouldFetchIdentityData() {
        // when
        final IdentityData returnedResult = lclIdentityFetcher.fetchIdentityData();

        // then
        assertThat(returnedResult.getFullName()).isEqualTo(PSU_NAME);
    }

    @Test
    public void shouldGetResponse() {
        // when
        final FetchIdentityDataResponse returnedResult = lclIdentityFetcher.response();

        // then
        assertThat(returnedResult.getIdentityData().getFullName()).isEqualTo(PSU_NAME);
    }
}
