package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.libraries.identitydata.IdentityData;

public class BnpParibasIdentityDataFetcherTest {

    private String connectedPsu;
    private BnpParibasIdentityDataFetcher bnpParibasIdentityDataFetcher;

    @Before
    public void init() {
        BnpParibasApiBaseClient apiClient = mock(BnpParibasApiBaseClient.class);
        EndUserIdentityResponse endUserIdentityResponse = mock(EndUserIdentityResponse.class);
        connectedPsu = "connectedPsu";

        when(apiClient.getEndUserIdentity()).thenReturn(endUserIdentityResponse);
        when(endUserIdentityResponse.getConnectedPsu()).thenReturn(connectedPsu);

        bnpParibasIdentityDataFetcher = new BnpParibasIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldFetchIdentityData() {
        // when
        IdentityData identityData = bnpParibasIdentityDataFetcher.fetchIdentityData();

        // then
        assertNotNull(identityData);
        assertEquals(connectedPsu, identityData.getFullName());
        assertNull(identityData.getDateOfBirth());
    }

    @Test
    public void shouldGetResponse() {
        // when
        FetchIdentityDataResponse fetchIdentityDataResponse =
                bnpParibasIdentityDataFetcher.response();

        // then
        assertNotNull(fetchIdentityDataResponse);
        assertNotNull(fetchIdentityDataResponse.getIdentityData());
        assertEquals(connectedPsu, fetchIdentityDataResponse.getIdentityData().getFullName());
        assertNull(fetchIdentityDataResponse.getIdentityData().getDateOfBirth());
    }
}
