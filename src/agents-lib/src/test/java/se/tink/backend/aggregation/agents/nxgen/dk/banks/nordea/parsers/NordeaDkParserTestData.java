package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parsers;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.rpc.TransactionsResponse;

public class NordeaDkParserTestData {

    public static TransactionsResponse parseTransaction() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(TEST_DATA, TransactionsResponse.class);
    }


    private static final String TEST_DATA = "{"
            + "\"getAccountTransactionsOut\": {"
            + "\"accountId\": {},"
            + "\"continueKey\": {},"
            + "\"accountTransaction\": {"
            + "\"transactionKey\": {},"
            + "\"transactionDate\": {"
            + "\"$\": \"2017-03-30\""
            + "},"
            + "\"transactionText\": {},"
            + "\"transactionCounterpartyName\": {"
            + "\"$\": \"Netbank gebyr  1.4.2016-31.3.2017\""
            + "},"
            + "\"transactionCurrency\": {"
            + "\"$\": \"DKK\""
            + "},"
            + "\"transactionAmount\": {"
            + "\"$\": -100.00"
            + "},"
            + "\"isCoverReservationTransaction\": {"
            + "\"$\": false"
            + "}"
            + "}"
            + "}"
            + "}";
}