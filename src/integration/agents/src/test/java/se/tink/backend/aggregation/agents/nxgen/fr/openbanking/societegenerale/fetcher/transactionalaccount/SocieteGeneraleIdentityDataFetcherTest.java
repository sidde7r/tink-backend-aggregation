package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.libraries.identitydata.IdentityData;

public class SocieteGeneraleIdentityDataFetcherTest {

    private SocieteGeneraleApiClient apiClient;
    private SocieteGeneraleIdentityDataFetcher societeGeneraleIdentityDataFetcher;
    private String connectedPsu = "connectedPsu";

    @Before
    public void init() {
        apiClient = mock(SocieteGeneraleApiClient.class);

        final EndUserIdentityResponse endUserIdentityResponse = mock(EndUserIdentityResponse.class);

        when(endUserIdentityResponse.getConnectedPsu()).thenReturn(connectedPsu);
        when(apiClient.getEndUserIdentity()).thenReturn(endUserIdentityResponse);

        societeGeneraleIdentityDataFetcher = new SocieteGeneraleIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldFetchIdentityData() {
        // given
        SocieteGeneraleIdentityDataFetcher societeGeneraleIdentityDataFetcher =
                new SocieteGeneraleIdentityDataFetcher(apiClient);

        // when
        IdentityData identityData = societeGeneraleIdentityDataFetcher.fetchIdentityData();

        // then
        assertNotNull(identityData);
        assertNull(identityData.getDateOfBirth());
        assertEquals(connectedPsu, identityData.getFullName());
    }

    @Test
    public void shouldGetResponse() {
        // given

        // when
        FetchIdentityDataResponse identityDataResponse =
                societeGeneraleIdentityDataFetcher.response();

        // then
        assertNotNull(identityDataResponse);
        assertNull(identityDataResponse.getIdentityData().getDateOfBirth());
        assertEquals(connectedPsu, identityDataResponse.getIdentityData().getFullName());
    }
}
