package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parsers;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser.NordeaDkParser;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser.NordeaDkTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.NordeaV20Parser;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class NordeaDkParserTest {
    @Test
    public void parseTransaction() throws Exception {
        NordeaV20Parser parser =
                new NordeaDkParser(new NordeaDkTransactionParser(), new Credentials());
        TransactionsResponse response = NordeaDkParserTestData.parseTransaction();

        Collection<Transaction> transactions =
                response.getTransactions().stream()
                        .map(parser::parseTransaction)
                        .collect(Collectors.toList());

        assertNotNull(transactions);
        assertTrue(1 == transactions.size());
        assertTrue(-100 == transactions.iterator().next().getAmount().getValue());
    }
}
