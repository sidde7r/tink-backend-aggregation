package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class CajamarInvestmentFetcherTest {
    static final String DATA_PATH = "data/test/agents/es/cajamar/";
    private CajamarApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(CajamarApiClient.class);
    }

    @Test
    public void shouldFetchCajamarInvestmentAccount() throws IOException {
        // given
        final PositionEntity position = loadSampleData("positions.json", PositionEntity.class);
        final InvestmentAccountResponse account =
                loadSampleData("investment.json", InvestmentAccountResponse.class);
        when(apiClient.getPositions()).thenReturn(Optional.ofNullable(position));
        when(apiClient.fetchInvestmentAccountDetails(any())).thenReturn(account);

        // when
        final CajamarInvestmentFetcher cajamarFetcher = new CajamarInvestmentFetcher(apiClient);
        final Collection<InvestmentAccount> creditCardAccounts = cajamarFetcher.fetchAccounts();

        // then
        Assert.assertEquals(1, creditCardAccounts.size());
    }

    @Test
    public void shouldFetchNullOrEmptyCajamarAccount() throws IOException {
        // given
        final PositionEntity position =
                loadSampleData("positions_without_cards.json", PositionEntity.class);
        when(apiClient.getPositions()).thenReturn(Optional.ofNullable(position));

        // when
        final CajamarInvestmentFetcher cajamarFetcher = new CajamarInvestmentFetcher(apiClient);
        final Collection<InvestmentAccount> investmentAccounts = cajamarFetcher.fetchAccounts();

        // then
        Assert.assertEquals(0, investmentAccounts.size());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
