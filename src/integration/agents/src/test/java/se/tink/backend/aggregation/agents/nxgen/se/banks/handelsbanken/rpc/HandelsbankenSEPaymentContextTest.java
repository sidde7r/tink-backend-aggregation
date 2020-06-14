package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc;

import static org.junit.Assert.*;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.executor.transfer.rpc.ConfirmTransferResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class HandelsbankenSEPaymentContextTest {
    @Test
    public void testParse() {
        Failure error = SerializationUtils.deserializeFromString(TEST_DATA, Failure.class);
        assertTrue(error.customerIsUnder16());
    }

    @Test
    public void testErrorResponse() {

        ErrorResponse errorResponse =
                SerializationUtils.deserializeFromString(TEST_DATA, ErrorResponse.class);
        assertEquals(403, errorResponse.getStatus());
    }

    @Test
    public void testMessag() {
        ConfirmTransferResponse messageable =
                SerializationUtils.deserializeFromString(TEST_DATA, ConfirmTransferResponse.class);
        assertEquals("10573", messageable.getCode());
    }

    static String TEST_DATA =
            "{\"type\":\"http://schemas.shbmain.shb.biz/http/status/clientError\",\"status\":403,\"detail\":\"Du måste ha fyllt 16 år för att kunna utföra denna tjänst.\",\"code\":\"10573\"}";
}
