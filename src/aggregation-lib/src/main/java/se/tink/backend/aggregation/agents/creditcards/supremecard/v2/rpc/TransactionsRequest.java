package se.tink.backend.aggregation.agents.creditcards.supremecard.v2.rpc;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import se.tink.backend.aggregation.agents.creditcards.supremecard.v2.SupremeCardApiConstants;

public class TransactionsRequest extends MultivaluedMapImpl {
    public static TransactionsRequest from(int year, int month) {
        TransactionsRequest transactionsRequest = new TransactionsRequest();
        transactionsRequest.add(SupremeCardApiConstants.TRANSACTIONS_REQUEST_YEAR_KEY, year);
        transactionsRequest.add(SupremeCardApiConstants.TRANSACTIONS_REQUEST_MONTH_KEY, month);

        return transactionsRequest;
    }
}
