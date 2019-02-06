package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.entities.BelfiusTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionDescriptionParserTest {
    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void assertThat_testMaestro1DescriptionParsing_succeeds() throws IOException {
        final String transactionString = TransactionTestData.maestroTransactionString1;
        BelfiusTransaction belfiusTransaction = mapper.readValue(transactionString, BelfiusTransaction.class);
        Transaction transaction = belfiusTransaction.toTinkTransaction();
        Assert.assertEquals("MERCHANT NAME PLACE", transaction.getDescription());
    }

    @Test
    public void assertThat_MaestroString2DescriptionParsing_succeeds() throws IOException {
        final String transactionString = TransactionTestData.maestroTransactionString2;
        BelfiusTransaction belfiusTransaction = mapper.readValue(transactionString, BelfiusTransaction.class);
        Transaction transaction = belfiusTransaction.toTinkTransaction();
        Assert.assertEquals("MERCHANT NAME", transaction.getDescription());
    }

    @Test
    public void assertThat_transactionParsing_succeeds() throws IOException {
        final String transactionString = TransactionTestData.transactionString;
        BelfiusTransaction belfiusTransaction = mapper.readValue(transactionString, BelfiusTransaction.class);
        Transaction transaction = belfiusTransaction.toTinkTransaction();

        Assert.assertEquals("MERCHANT NAME", transaction.getDescription());
        Assert.assertEquals(new Double(-10), transaction.getAmount().getValue());
        Assert.assertEquals("{\"recipientAccount\":[\"MERCHANT NAME\"],\"details\":[\"TEXT TEXT TEXT TEXT 123456789 TEXT TEXT   \\nTEXT TEXT: MORE TEXT    \\ntext text 1234567 03/14 TEXT:\\n1234567890                        \\nREF. : 123456789 VAL. 12-34                       \\n\"]}", transaction.getRawDetails());
    }
}
