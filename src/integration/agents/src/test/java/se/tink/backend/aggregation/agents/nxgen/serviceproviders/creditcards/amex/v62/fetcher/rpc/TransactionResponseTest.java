package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.creditcards.amex.v62.AmericanExpressV62SEConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.AmericanExpressV62Configuration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.rpc.TransactionResponseTestDataHelper.ResponseType;

public class TransactionResponseTest {
    AmericanExpressV62Configuration config = new AmericanExpressV62SEConfiguration();

    @Test
    public void parseResponse_properTransactionList() throws IOException {
        TransactionResponse transactionResponse =
                TransactionResponseTestDataHelper.buildResponse(
                        TransactionResponseTestDataHelper.ResponseType.PROPER_TRANSACTION_LIST);
        Collection transactions = transactionResponse.toTinkTransactions(config, false, "01");
        assertFalse(transactions.isEmpty());
        assertEquals(1, transactions.size());
    }

    @Test
    public void parseResponse_emptyTransactionList() throws IOException {
        TransactionResponse transactionResponse =
                TransactionResponseTestDataHelper.buildResponse(
                        TransactionResponseTestDataHelper.ResponseType.NO_TRANSACTIONS_FOR_PERIOD);

        Collection transactions = transactionResponse.toTinkTransactions(config, false, "01");
        assertTrue(transactions.isEmpty());
    }

    @Test
    public void parseResponse_errorPageOfTransactionList() throws IOException {
        TransactionResponse transactionResponse =
                TransactionResponseTestDataHelper.buildResponse(
                        TransactionResponseTestDataHelper.ResponseType.ERROR_LIST);
        boolean canFetchMore = transactionResponse.canFetchMore();

        assertFalse(canFetchMore);
    }

    @Test
    public void canFetchMore_emptyTransactionListForPeriod() throws IOException {
        TransactionResponse transactionResponse =
                TransactionResponseTestDataHelper.buildResponse(
                        ResponseType.NO_TRANSACTIONS_FOR_PERIOD);
        boolean canFetchMore = transactionResponse.canFetchMore();
        assertFalse(canFetchMore);
    }

    @Test
    public void canFetchMore_properTransactionList() throws IOException {
        TransactionResponse transactionResponse =
                TransactionResponseTestDataHelper.buildResponse(
                        TransactionResponseTestDataHelper.ResponseType.PROPER_TRANSACTION_LIST);
        boolean canFetchMore = transactionResponse.canFetchMore();
        assertTrue(canFetchMore);
    }
}
