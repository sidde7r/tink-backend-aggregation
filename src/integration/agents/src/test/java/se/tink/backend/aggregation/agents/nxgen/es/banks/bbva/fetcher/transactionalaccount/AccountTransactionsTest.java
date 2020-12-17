package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.jackson.datatype.VavrModule;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities.AccountTransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class AccountTransactionsTest {

    static final String DATA_PATH = "data/test/agents/es/bbva/";

    private <T> T loadSampleData(String path, Class<T> cls) throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new VavrModule());
        return objectMapper.readValue(Paths.get(DATA_PATH, path).toFile(), cls);
    }

    @Test
    public void testAccountTransactionsParsing() throws IOException {
        final AccountTransactionEntity accountTransactionEntity =
                loadSampleData("account_transaction.json", AccountTransactionEntity.class);

        Transaction transaction = accountTransactionEntity.toTransaction();

        Assert.assertEquals(BigDecimal.valueOf(-13.07), transaction.getAmount().getExactValue());
        Assert.assertEquals("EUR", transaction.getExactAmount().getCurrencyCode());
        Assert.assertEquals(
                "LIQUIDACION DE INTERESES-COMISIONES-GASTOS", transaction.getDescription());
    }
}
