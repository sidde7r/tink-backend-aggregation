package se.tink.libraries.metrics.utils;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MetricsUtilsTest {

    @Test
    public void testCleanMetricName() {
        String input = "'test*'-type(bankid)swedbank";

        String metricName = MetricsUtils.cleanMetricName(input);
        assertEquals("test-type_bankid_swedbank", metricName);
    }
}
