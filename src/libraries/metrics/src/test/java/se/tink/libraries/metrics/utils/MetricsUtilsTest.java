package se.tink.libraries.metrics.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MetricsUtilsTest {

    @Test
    public void testCleanMetricName() {
        String input = "'test*'-type(bankid)swedbank";

        String metricName = MetricsUtils.cleanMetricName(input);
        assertEquals("test-type_bankid_swedbank", metricName);
    }
}
