package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.identity.dto.EndUserIdentityResponseDto;
import se.tink.libraries.identitydata.IdentityData;

public class LaBanquePostaleIdentityDataFetcherTest {

    private static final String PSU_NAME = "DUMMY_NAME";

    private LaBanquePostaleIdentityDataFetcher identityDataFetcher;

    @Before
    public void init() {
        final LaBanquePostaleApiClient apiClient = createLaBanquePostaleApiClientMock();

        identityDataFetcher = new LaBanquePostaleIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldFetchIdentityData() {
        // when
        final IdentityData response = identityDataFetcher.fetchIdentityData();

        // then
        assertThat(response.getFullName()).isEqualTo(PSU_NAME);
    }

    @Test
    public void shouldGetResponse() {
        // when
        final FetchIdentityDataResponse response = identityDataFetcher.response();

        // then
        assertThat(response.getIdentityData().getFullName()).isEqualTo(PSU_NAME);
    }

    private static LaBanquePostaleApiClient createLaBanquePostaleApiClientMock() {
        final EndUserIdentityResponseDto endUserIdentityResponseDtoMock =
                mock(EndUserIdentityResponseDto.class);
        when(endUserIdentityResponseDtoMock.getConnectedPSU()).thenReturn(PSU_NAME);

        final LaBanquePostaleApiClient apiClient = mock(LaBanquePostaleApiClient.class);
        when(apiClient.getEndUserIdentity()).thenReturn(endUserIdentityResponseDtoMock);

        return apiClient;
    }
}
