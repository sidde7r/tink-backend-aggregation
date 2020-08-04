package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.identitydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IdentityDataFetcherTest {

    private static final String IDENTITY_DATA_FILE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/nordea/resources/identityData.json";
    private static final IdentityDataResponse IDENTITY_DATA_RESPONSE =
            SerializationUtils.deserializeFromString(
                    new File(IDENTITY_DATA_FILE_PATH), IdentityDataResponse.class);

    @Test
    public void shouldReturnProperlyMappedIdentityData() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        given(fetcherClient.fetchIdentityData()).willReturn(IDENTITY_DATA_RESPONSE);
        IdentityDataFetcher identityDataFetcher = new IdentityDataFetcher(fetcherClient);

        // when
        IdentityData identityData = identityDataFetcher.fetchIdentityData();

        // then
        assertThat(identityData.getDateOfBirth()).isEqualTo(LocalDate.of(1922, 1, 29));
        assertThat(identityData.getFullName()).isEqualTo("First Second Surname");
        assertThat(identityData.getNameElements()).hasSize(2);
    }
}
