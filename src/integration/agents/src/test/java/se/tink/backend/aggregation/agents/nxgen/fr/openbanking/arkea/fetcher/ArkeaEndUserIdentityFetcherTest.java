package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher.ArkeaFetcherFixtures.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.libraries.identitydata.IdentityData;

@RunWith(MockitoJUnitRunner.class)
public class ArkeaEndUserIdentityFetcherTest {

    @Mock private ArkeaApiClient apiClient;
    private ArkeaEndUserIdentityFetcher endUserIdentityFetcher;

    @Before
    public void setUp() {
        endUserIdentityFetcher = new ArkeaEndUserIdentityFetcher(apiClient);
    }

    @Test
    public void shouldFetchEndUserIdentity() {

        // given
        when(apiClient.getUserIdentity()).thenReturn(END_USER_IDENTITY_RESPONSE);

        // when
        IdentityData identityData = endUserIdentityFetcher.fetchIdentityData();

        // then
        assertThat(identityData.getFullName()).isEqualTo("IMeMyself");
    }
}
