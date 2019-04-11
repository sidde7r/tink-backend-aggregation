package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc;

import static org.junit.Assert.*;

import org.junit.Test;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class HandelsbankenSEPaymentContextTest {
    @Test
    public void testParse() {
        Failure error = SerializationUtils.deserializeFromString(TEST_DATA, Failure.class);
        assertTrue(error.customerIsUnder16());
    }

    static String TEST_DATA =
            "{\"type\":\"http://schemas.shbmain.shb.biz/http/status/clientError\",\"status\":403,\"detail\":\"Du måste ha fyllt 16 år för att kunna utföra denna tjänst.\",\"code\":\"10573\"}";
}
