package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.identitydata;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarConstants.SessionKeys;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.identitydata.rpc.CajamarIdentityDataResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class CajamarIdentityDataFetcherTest {

    private static final String DATA_PATH = "data/test/agents/es/cajamar/";
    private CajamarApiClient apiClient;
    private SessionStorage sessionStorage;

    @Before
    public void setup() {
        apiClient = mock(CajamarApiClient.class);
        sessionStorage = mock(SessionStorage.class);
    }

    @Test
    public void shouldFetchCajamarIdentity() throws IOException {
        // given
        final PositionEntity position = loadSampleData("positions.json", PositionEntity.class);
        final CajamarIdentityDataResponse identityDataResponse =
                loadSampleData("identity.json", CajamarIdentityDataResponse.class);
        when(apiClient.fetchPositions()).thenReturn(position);
        when(apiClient.fetchIdentityData(any())).thenReturn(identityDataResponse);
        when(sessionStorage.get(SessionKeys.ACCOUNT_HOLDER_NAME)).thenReturn("ALEJANDRO ROBERTO");

        // when
        final CajamarIdentityDataFetcher cajamarIdentityDataFetcher =
                new CajamarIdentityDataFetcher(apiClient, sessionStorage);
        final IdentityData identityData = cajamarIdentityDataFetcher.fetchIdentityData();

        // then
        Assert.assertEquals("00111111A", identityData.getSsn());
        Assert.assertEquals("ALEJANDRO ROBERTO", identityData.getFullName());
    }

    @Test
    public void shouldFetchEmptyCajamarIdentity() throws IOException {
        // given
        final PositionEntity position = loadSampleData("positions.json", PositionEntity.class);
        final CajamarIdentityDataResponse identityDataResponse =
                loadSampleData("unmapped_identity.json", CajamarIdentityDataResponse.class);

        when(apiClient.fetchPositions()).thenReturn(position);
        when(apiClient.fetchIdentityData(any())).thenReturn(identityDataResponse);
        when(sessionStorage.get(SessionKeys.ACCOUNT_HOLDER_NAME)).thenReturn("GONZALEZ MYSZOJELEN");

        // when
        final CajamarIdentityDataFetcher cajamarIdentityDataFetcher =
                new CajamarIdentityDataFetcher(apiClient, sessionStorage);
        final IdentityData identityData = cajamarIdentityDataFetcher.fetchIdentityData();

        // then
        Assert.assertEquals("", identityData.getSsn());
        Assert.assertEquals("GONZALEZ MYSZOJELEN", identityData.getFullName());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
