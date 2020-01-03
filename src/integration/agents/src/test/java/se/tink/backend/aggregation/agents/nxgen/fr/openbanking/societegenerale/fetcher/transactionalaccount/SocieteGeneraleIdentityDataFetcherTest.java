package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount;

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
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class SocieteGeneraleIdentityDataFetcherTest {

    private SocieteGeneraleApiClient apiClient;
    private SocieteGeneraleConfiguration configuration;
    private SessionStorage sessionStorage;
    private SignatureHeaderProvider signatureHeaderProvider;
    private EndUserIdentityResponse endUserIdentityResponse;
    private EidasProxyConfiguration eidasProxyConfiguration;
    private EidasIdentity eidasIdentity;
    private SocieteGeneraleIdentityDataFetcher societeGeneraleIdentityDataFetcher;
    String connectedPsu = "connectedPsu";

    @Before
    public void init() {
        apiClient = mock(SocieteGeneraleApiClient.class);
        configuration = mock(SocieteGeneraleConfiguration.class);
        sessionStorage = mock(SessionStorage.class);
        signatureHeaderProvider = mock(SignatureHeaderProvider.class);
        endUserIdentityResponse = mock(EndUserIdentityResponse.class);
        eidasProxyConfiguration = mock(EidasProxyConfiguration.class);
        eidasIdentity = mock(EidasIdentity.class);

        String token = "token";
        String signature = "signature";

        when(sessionStorage.get(SocieteGeneraleConstants.StorageKeys.TOKEN)).thenReturn(token);
        when(signatureHeaderProvider.buildSignatureHeader(
                        any(), any(), anyString(), anyString(), any()))
                .thenReturn(signature);
        when(endUserIdentityResponse.getConnectedPsu()).thenReturn(connectedPsu);
        when(apiClient.getEndUserIdentity(anyString(), anyString()))
                .thenReturn(endUserIdentityResponse);

        societeGeneraleIdentityDataFetcher =
                new SocieteGeneraleIdentityDataFetcher(
                        apiClient,
                        configuration,
                        sessionStorage,
                        signatureHeaderProvider,
                        eidasProxyConfiguration,
                        eidasIdentity);
    }

    @Test
    public void shouldFetchIdentityData() {
        // given
        SocieteGeneraleIdentityDataFetcher societeGeneraleIdentityDataFetcher =
                new SocieteGeneraleIdentityDataFetcher(
                        apiClient,
                        configuration,
                        sessionStorage,
                        signatureHeaderProvider,
                        eidasProxyConfiguration,
                        eidasIdentity);

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
