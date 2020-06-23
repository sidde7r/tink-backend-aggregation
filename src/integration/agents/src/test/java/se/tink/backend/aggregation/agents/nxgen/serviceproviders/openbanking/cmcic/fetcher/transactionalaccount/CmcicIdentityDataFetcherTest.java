package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.libraries.identitydata.IdentityData;

public class CmcicIdentityDataFetcherTest {

    private EndUserIdentityResponse endUserIdentityResponse;
    private CmcicIdentityDataFetcher cmcicIdentityDataFetcher;

    @Before
    public void init() {
        String connectedPsu = "connectedPsu";
        CmcicApiClient apiClient = mock(CmcicApiClient.class);
        endUserIdentityResponse = mock(EndUserIdentityResponse.class);

        when(apiClient.getEndUserIdentity()).thenReturn(endUserIdentityResponse);
        when(endUserIdentityResponse.getConnectedPsu()).thenReturn(connectedPsu);

        cmcicIdentityDataFetcher = new CmcicIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldFetchIdentityData() {
        // given

        // when
        IdentityData response = cmcicIdentityDataFetcher.fetchIdentityData();

        // then
        assertNotNull(response);
        assertEquals(endUserIdentityResponse.getConnectedPsu(), response.getFullName());
        assertNull(response.getDateOfBirth());
    }

    @Test
    public void shouldGetResponse() {
        // given

        // when
        FetchIdentityDataResponse response = cmcicIdentityDataFetcher.response();

        // then
        assertNotNull(response);
        assertEquals(
                endUserIdentityResponse.getConnectedPsu(),
                response.getIdentityData().getFullName());
        assertNull(response.getIdentityData().getDateOfBirth());
    }
}
