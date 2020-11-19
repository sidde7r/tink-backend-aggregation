package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.entercard.fetcher.entities.TransactionKey;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

import java.io.IOException;
import java.util.Collection;

import static org.junit.Assert.*;

public class CreditCardTransactionsResponseTest {

    private static final String CREDIT_CARD_TRANSACTIONS_RESPONSE_VALID_JSON =
            "{\"metadata\": {\"offset\":1, \"result_count\": 1, \"total_count\": 2}," +
                    " \"account\": {\"movements\": [{\"timeOfPurchase\": \"2020-01-23\", " +
                    "\"merchantName\": \"merchantName\", \"billingCurrency\": \"EUR\", " +
                    "\"billingAmount\": 30, \"movementStatus\": \"OTHER\"}]}}";
    private static final String CREDIT_CARD_TRANSACTIONS_RESPONSE_OPEN_JSON =
            "{\"metadata\": {\"offset\":1, \"result_count\": 1, \"total_count\": 1}," +
                    " \"account\": {\"movements\": [{\"timeOfPurchase\": \"2020-01-23\", " +
                    "\"merchantName\": \"merchantName\", \"billingCurrency\": \"EUR\", " +
                    "\"billingAmount\": 30, \"movementStatus\": \"OPEN\"}]}}";

    private static final CreditCardTransactionsResponse CREDIT_CARD_TRANSACTIONS_RESPONSE_VALID =
            getCreditCardTransactionsResponse(CREDIT_CARD_TRANSACTIONS_RESPONSE_VALID_JSON);
    private static final CreditCardTransactionsResponse CREDIT_CARD_TRANSACTION_OPEN =
            getCreditCardTransactionsResponse(CREDIT_CARD_TRANSACTIONS_RESPONSE_OPEN_JSON);

    @Test
    public void testGetTinkTransactionsWithOTHERStatus() {
        Collection<? extends Transaction> result = CREDIT_CARD_TRANSACTIONS_RESPONSE_VALID.getTinkTransactions();
        assertEquals(1, result.size());
        CreditCardTransaction creditCardTransaction = (CreditCardTransaction) result.toArray()[0];
        assertFalse(creditCardTransaction.isPending());
    }

    @Test
    public void testGetTinkTransactionsWithOPENStatus() {
        Collection<? extends Transaction> result = CREDIT_CARD_TRANSACTION_OPEN.getTinkTransactions();
        assertEquals(0, result.size());
    }

    @Test
    public void testNextKeyWithCanFetchTrue() {
        TransactionKey result = CREDIT_CARD_TRANSACTIONS_RESPONSE_VALID.nextKey();
        assertEquals(result.getStartAtRowNum(), 2);
        assertEquals(result.getStopAfterRowNum(), 101);
    }

    @Test
    public void testNextKeyWithCanFetchFalse() {
        assertNull(CREDIT_CARD_TRANSACTION_OPEN.nextKey());
    }

    private static CreditCardTransactionsResponse getCreditCardTransactionsResponse(final String json) {
        try {
            return new ObjectMapper().readValue(json, CreditCardTransactionsResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
