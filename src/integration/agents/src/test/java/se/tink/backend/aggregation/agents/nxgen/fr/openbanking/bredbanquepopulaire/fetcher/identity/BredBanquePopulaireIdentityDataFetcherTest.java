package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.FULL_NAME;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.BredBanquePopulaireTestFixtures.getEndUserIdentityResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.identity.rpc.EndUserIdentityResponse;
import se.tink.libraries.identitydata.IdentityData;

@RunWith(MockitoJUnitRunner.class)
public class BredBanquePopulaireIdentityDataFetcherTest {

    @Mock private BredBanquePopulaireApiClient apiClient;
    private BredBanquePopulaireIdentityDataFetcher bredBanquePopulaireIdentityDataFetcher;

    @Before
    public void setUp() {
        final EndUserIdentityResponse endUserIdentityResponse = getEndUserIdentityResponse();
        when(apiClient.getEndUserIdentity()).thenReturn(endUserIdentityResponse);

        bredBanquePopulaireIdentityDataFetcher =
                new BredBanquePopulaireIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldReturnEndUserIdentity() {
        // when
        IdentityData identityData = bredBanquePopulaireIdentityDataFetcher.fetchIdentityData();

        // then
        assertThat(identityData).isNotNull();
        assertThat(identityData.getFullName()).isEqualTo(FULL_NAME);
    }
}
