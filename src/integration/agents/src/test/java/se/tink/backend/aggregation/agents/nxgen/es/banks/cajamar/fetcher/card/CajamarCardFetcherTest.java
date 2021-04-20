package se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.card;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.CajamarApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.entities.PositionEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.CajamarCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.cajamar.fetcher.creditcard.rpc.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class CajamarCardFetcherTest {
    static final String DATA_PATH = "data/test/agents/es/cajamar/";
    private CajamarApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(CajamarApiClient.class);
    }

    @Test
    public void shouldFetchCajamarAccount() throws IOException {
        // given
        final PositionEntity position = loadSampleData("positions.json", PositionEntity.class);
        final CreditCardResponse account = loadSampleData("card.json", CreditCardResponse.class);
        when(apiClient.fetchPositions()).thenReturn(position);
        when(apiClient.fetchCreditCardDetails(any())).thenReturn(account);

        // when
        final CajamarCreditCardFetcher cajamarCardFetcher = new CajamarCreditCardFetcher(apiClient);
        final Collection<CreditCardAccount> creditCardAccounts = cajamarCardFetcher.fetchAccounts();

        // then
        Assert.assertEquals(1, creditCardAccounts.size());
    }

    @Test
    public void shouldFetchNullOrEmptyCajamarAccount() throws IOException {
        // given
        final PositionEntity position =
                loadSampleData("positions_without_cards.json", PositionEntity.class);
        when(apiClient.fetchPositions()).thenReturn(position);

        // when
        final CajamarCreditCardFetcher cajamarCardFetcher = new CajamarCreditCardFetcher(apiClient);
        final Collection<CreditCardAccount> creditCardAccounts = cajamarCardFetcher.fetchAccounts();

        // then
        Assert.assertEquals(0, creditCardAccounts.size());
    }

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }
}
