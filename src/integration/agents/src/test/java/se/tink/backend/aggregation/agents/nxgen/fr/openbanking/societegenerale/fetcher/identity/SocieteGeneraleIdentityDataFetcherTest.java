package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.libraries.identitydata.IdentityData;

@RunWith(MockitoJUnitRunner.class)
public class SocieteGeneraleIdentityDataFetcherTest {

    @Mock private SocieteGeneraleApiClient apiClient;
    private SocieteGeneraleIdentityDataFetcher societeGeneraleIdentityDataFetcher;
    private String connectedPsu = "connectedPsu";

    @Before
    public void init() {
        societeGeneraleIdentityDataFetcher = new SocieteGeneraleIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldFetchIdentityData() {
        // given
        final EndUserIdentityResponse endUserIdentityResponse = mock(EndUserIdentityResponse.class);
        when(endUserIdentityResponse.getConnectedPsu()).thenReturn(connectedPsu);
        when(apiClient.getEndUserIdentity()).thenReturn(endUserIdentityResponse);

        // when
        IdentityData identityData = societeGeneraleIdentityDataFetcher.fetchIdentityData();

        // then
        assertThat(identityData.getFullName()).isEqualTo(connectedPsu);
    }
}
