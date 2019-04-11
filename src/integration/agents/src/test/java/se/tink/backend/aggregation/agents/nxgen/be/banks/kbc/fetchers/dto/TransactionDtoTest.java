package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionDtoTest {

    private String transactionTestString =
            "{"
                    + "\"transactionDate\": "
                    + "{\"V\": \"190318\","
                    + "\"T\": \"date\"},"
                    + "\"amount\": "
                    + "{\"V\": \"-100.50\","
                    + "\"T\": \"decimal\"},"
                    + "\"appendixType\": "
                    + "{\"V\": \"0\","
                    + "\"T\": \"text\"},"
                    + "\"descriptionLine01\": "
                    + "{\"V\": \"FIRSTNAME LASTNAME\","
                    + "\"T\": \"text\"},"
                    + "\"descriptionLine02\": "
                    + "{\"V\": \"Credit transfer\","
                    + "\"T\": \"text\"},"
                    + "\"subAccountNo\": "
                    + "{\"V\": \"000\","
                    + "\"T\": \"text\"},"
                    + "\"registrationTs\": "
                    + "{\"V\": \"2018-03-19-12.34.07.463626\","
                    + "\"T\": \"text\"},"
                    + "\"bookingDate\": "
                    + "{\"V\": \"20032018\","
                    + "\"T\": \"date\"}"
                    + "}";

    @Test
    public void testTransactionDtoParsing() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TransactionDto transactionDto =
                mapper.readValue(transactionTestString, TransactionDto.class);
        Transaction tinkTransaction = transactionDto.toTinkTransaction();

        assertEquals(tinkTransaction.getAmount().getValue(), new Double(-100.50d));
        assertEquals(tinkTransaction.getDescription(), "FIRSTNAME LASTNAME");
        assertEquals(tinkTransaction.getRawDetails(), "{\"details\":[\"Credit transfer\"]}");
    }
}
