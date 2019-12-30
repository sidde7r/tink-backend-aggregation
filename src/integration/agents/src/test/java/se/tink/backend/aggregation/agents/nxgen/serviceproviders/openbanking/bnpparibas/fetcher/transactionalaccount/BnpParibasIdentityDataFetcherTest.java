package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasApiBaseClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration.BnpParibasConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.utils.BnpParibasSignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class BnpParibasIdentityDataFetcherTest {

    private BnpParibasApiBaseClient apiClient;
    private SessionStorage sessionStorage;
    private BnpParibasSignatureHeaderProvider bnpParibasSignatureHeaderProvider;
    private String token;
    private BnpParibasConfiguration bnpParibasConfiguration;
    private String signature;
    private EndUserIdentityResponse endUserIdentityResponse;
    private String connectedPsu;
    private BnpParibasIdentityDataFetcher bnpParibasIdentityDataFetcher;

    @Before
    public void init() {
        apiClient = mock(BnpParibasApiBaseClient.class);
        sessionStorage = mock(SessionStorage.class);
        bnpParibasSignatureHeaderProvider = mock(BnpParibasSignatureHeaderProvider.class);
        token = "token";
        bnpParibasConfiguration = mock(BnpParibasConfiguration.class);
        signature = "signature";
        endUserIdentityResponse = mock(EndUserIdentityResponse.class);
        connectedPsu = "connectedPsu";

        when(sessionStorage.get(BnpParibasBaseConstants.StorageKeys.TOKEN)).thenReturn(token);
        when(apiClient.getBnpParibasConfiguration()).thenReturn(bnpParibasConfiguration);
        when(bnpParibasSignatureHeaderProvider.buildSignatureHeader(
                        any(), any(), any(), any(), any()))
                .thenReturn(signature);
        when(apiClient.getEndUserIdentity(anyString(), anyString()))
                .thenReturn(endUserIdentityResponse);
        when(endUserIdentityResponse.getConnectedPsu()).thenReturn(connectedPsu);

        bnpParibasIdentityDataFetcher =
                new BnpParibasIdentityDataFetcher(
                        apiClient, sessionStorage, bnpParibasSignatureHeaderProvider);
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
