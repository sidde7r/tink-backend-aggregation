package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity.rpc.IdentityResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BoursoramaIdentityDataFetcherTest {

    private static final String IDENTITY_DATA_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/fr/banks/boursorama/resources/identity_data_response.json";

    private BoursoramaIdentityDataFetcher identityDataFetcher;
    private BoursoramaApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(BoursoramaApiClient.class);
        identityDataFetcher = new BoursoramaIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldFetchIdentity() {
        // given
        when(apiClient.getIdentityData())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(IDENTITY_DATA_FILE_PATH), IdentityResponse.class));
        // when
        FetchIdentityDataResponse identityDataResponse = identityDataFetcher.fetchIdentityData();
        // then
        assertThat(identityDataResponse.getIdentityData().getFullName()).isEqualTo("Test User");
        assertThat(identityDataResponse.getIdentityData().getDateOfBirth()).isEqualTo("1945-07-17");
    }
}
