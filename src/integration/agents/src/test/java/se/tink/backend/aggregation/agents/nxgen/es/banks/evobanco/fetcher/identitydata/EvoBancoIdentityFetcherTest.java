package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.rpc.EvoBancoIdentityDataResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EvoBancoIdentityFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/evobanco/resources";
    private EvoBancoApiClient apiClient;
    private EvoBancoIdentityDataFetcher evoBancoIdentityDataFetcher;

    @Before
    public void setup() {
        apiClient = mock(EvoBancoApiClient.class);
        evoBancoIdentityDataFetcher = new EvoBancoIdentityDataFetcher(apiClient);
    }

    @Test
    public void shouldFetchIdentityData() {
        // given
        when(apiClient.fetchIdentityData())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "identity_data.json").toFile(),
                                EvoBancoIdentityDataResponse.class));

        // when
        IdentityData identityData = evoBancoIdentityDataFetcher.fetchIdentityData();

        // then
        Assert.assertEquals(identityData.getDateOfBirth().toString(), "1999-01-01");
        Assert.assertEquals(identityData.getFullName(), "ROBERTO ALEJANDRO VALDES");
    }
}
