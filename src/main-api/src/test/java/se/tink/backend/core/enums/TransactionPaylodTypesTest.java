package se.tink.backend.core.enums;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionPayloadTypes;

public class TransactionPaylodTypesTest {

    private static final String TRANSACTION_PAYLOAD = "{\"GIRO\" : \"someString\",\"TRANSFER_TWIN\" : \"someString\",\"TRANSFER_ACCOUNT\" : \"someString\",\"TRANSFER_ACCOUNT_EXTERNAL\" : \"someString\",\"TRANSFER_ACCOUNT_NAME_EXTERNAL\" : \"someString\",\"EXTERNAL_ID\" : \"someString\",\"VERIFICATION_NUMBER\" : \"someString\",\"CHARGEBACK_OR_RETURN\" : \"someString\",\"UNSETTLED_AMOUNT\" : \"someString\",\"DOCUMENT\" : \"someString\",\"PAYMENT_GATEWAY\" : \"someString\",\"AGENT_CATEGORY\" : \"someString\",\"AGENT_ORIGINAL_CATEGORY\" : \"someString\",\"INTUIT_CATEGORY\" : \"someString\",\"MCC\" : \"someString\",\"YODLEE_CATEGORY\" : \"someString\",\"AMEX_CATEGORY\" : \"someString\",\"GOOGLE_PLACES_TYPES\" : \"someString\",\"SUB_ACCOUNT\" : \"someString\",\"TRANSFER_PROVIDER\" : \"someString\",\"FRAUD_STATUS\" : \"someString\",\"MESSAGE\" : \"someString\",\"EDITABLE_TRANSACTION_TRANSFER_ID\" : \"someString\",\"EDITABLE_TRANSACTION_TRANSFER\" : \"someString\"}";
    private static final String TRANSACTION_INTERNAL_PAYLOAD = "{\"INCOMING_TIMESTAMP\" : \"someString\"}";

    @Test
    public void validateCorrectNumberOfProperties() {
        Transaction transaction = new Transaction();
        transaction.setPayloadSerialized(TRANSACTION_PAYLOAD);

        for (TransactionPayloadTypes paylod : transaction.getPayload().keySet()) {
            Assert.assertEquals("someString", transaction.getPayloadValue(paylod));
        }

        Assert.assertEquals(24, transaction.getPayload().values().size());
        Assert.assertEquals(0, transaction.getInternalPayload().values().size());
    }

    @Test
    public void testInternalPayload() {
        Transaction transaction = new Transaction();
        transaction.setInternalPayloadSerialized(TRANSACTION_INTERNAL_PAYLOAD);

        for (String paylod : transaction.getInternalPayload().keySet()) {
            Assert.assertEquals("someString", transaction.getInternalPayload(paylod));
        }

        Assert.assertEquals(0, transaction.getPayload().values().size());
        Assert.assertEquals(1, transaction.getInternalPayload().values().size());
    }
}
