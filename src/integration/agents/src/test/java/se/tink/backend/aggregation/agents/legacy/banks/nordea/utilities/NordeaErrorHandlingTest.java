package se.tink.backend.aggregation.agents.banks.nordea.utilities;

import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.BankingServiceResponse;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.TransferResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@SuppressWarnings("unchecked")
public class NordeaErrorHandlingTest {

    @Test
    public void testFindCode() {
        String onTheWire = "{\"errorCode\":{\"$\":\"MBS0528\"}}";

        BankingServiceResponse response = new BankingServiceResponse();
        response.setErrorMessage((Map<String, Object>) SerializationUtils
                .deserializeFromString(onTheWire, Object.class));

        Assert.assertTrue(response.getErrorCode().isPresent());
        Assert.assertEquals("MBS0528", response.getErrorCode().get());
    }

    @Test
    public void testNoCode() {
        String onTheWire = "{\"errorCode\":{}}";

        BankingServiceResponse response = new BankingServiceResponse();
        response.setErrorMessage((Map<String, Object>) SerializationUtils
                .deserializeFromString(onTheWire, Object.class));

        Assert.assertFalse(response.getErrorCode().isPresent());
    }

    @Test
    public void testNoError() {
        String onTheWire = "{}";

        BankingServiceResponse response = new BankingServiceResponse();
        response.setErrorMessage((Map<String, Object>) SerializationUtils
                .deserializeFromString(onTheWire, Object.class));

        Assert.assertFalse(response.getErrorCode().isPresent());
    }

    /**
     * There could be a error with INFO level, let's not call this an error.
     */
    @Test
    public void testNoError2() {
        String onTheWire = "{\"createPaymentOut\":{\"paymentSubType\":{\"$\":\"Normal\"},\"paymentSubTypeExtension\":"
                + "{\"$\":\"BGType\"},\"dueDateType\":{},\"paymentDate\":{\"$\":\"2016-03-21T12:00:00.220+01:00\"},"
                + "\"statusCode\":{\"$\":\"Unconfirmed\"},\"paymentToken\":{},\"warningText\":{},"
                + "\"advancedSigningRequested\":{\"$\":false},\"challenge\":{},\"encryptedPaymentData\":{},"
                + "\"error\":{\"errorId\":{\"$\":\"MBS0511\"},\"errorLevel\":{\"$\":\"INFO\"}}}}";

        TransferResponse response = SerializationUtils.deserializeFromString(onTheWire, TransferResponse.class);

        Assert.assertFalse(response.getError().isPresent());
    }

}
