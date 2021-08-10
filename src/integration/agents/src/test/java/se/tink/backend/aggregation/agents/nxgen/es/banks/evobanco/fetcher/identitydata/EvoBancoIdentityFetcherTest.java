package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.identitydata.rpc.EvoBancoIdentityDataResponse;
import se.tink.libraries.identitydata.IdentityData;

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
    public void shouldFetchIdentityData() throws IOException {
        // given
        EvoBancoIdentityDataResponse response =
                loadSampleData("identity_data.json", EvoBancoIdentityDataResponse.class);

        when(apiClient.fetchIdentityData()).thenReturn(response);

        // when
        IdentityData identityData = evoBancoIdentityDataFetcher.fetchIdentityData();

        // then
        Assert.assertEquals(identityData.getDateOfBirth().toString(), "1970-01-01");
        Assert.assertEquals(identityData.getFullName(), "ALEJANDRO VALDES ALCANTARA");
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(TEST_DATA_PATH, path).toFile(), cls);
    }
}
