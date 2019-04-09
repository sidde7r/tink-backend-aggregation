package se.tink.libraries.metrics;

import com.google.common.collect.ImmutableList;
import io.prometheus.client.Collector;
import io.prometheus.client.exporter.common.TextFormat;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PrometheusMetricCollectorTest {

    private MetricCollector collector;
    private MetricRegistry registry;

    @Before
    public void setup() {
        collector = new MetricCollector();
        registry = new MetricRegistry(collector);
    }

    @Test
    public void testCollect() throws IOException {
        Counter counter = registry.meter(MetricId.newId("test_counter"));
        Timer timer =
                registry.timer(
                        MetricId.newId("test_timer").label("label_name", "label_value"),
                        ImmutableList.of(1, 10, 100));

        counter.inc();
        timer.update(10, TimeUnit.SECONDS);

        List<Collector.MetricFamilySamples> samplesList = collector.collect();
        StringWriter output = new StringWriter();
        TextFormat.write004(output, Collections.enumeration(samplesList));

        Assert.assertEquals(
                "# HELP tink_test_counter_total TODO: No help available\n"
                        + "# TYPE tink_test_counter_total counter\n"
                        + "tink_test_counter_total 1.0\n"
                        + "# HELP tink_test_timer_seconds TODO: No help available\n"
                        + "# TYPE tink_test_timer_seconds histogram\n"
                        + "tink_test_timer_seconds_count{label_name=\"label_value\",} 1.0\n"
                        + "tink_test_timer_seconds_sum{label_name=\"label_value\",} 10.0\n"
                        + "tink_test_timer_seconds_bucket{label_name=\"label_value\",le=\"1.0\",} 0.0\n"
                        + "tink_test_timer_seconds_bucket{label_name=\"label_value\",le=\"10.0\",} 1.0\n"
                        + "tink_test_timer_seconds_bucket{label_name=\"label_value\",le=\"100.0\",} 1.0\n"
                        + "tink_test_timer_seconds_bucket{label_name=\"label_value\",le=\"+Inf\",} 1.0\n",
                output.toString());
    }

    @Test
    public void ensureMetricNameWith_underscore_and_lowercase_isAllowed() {
        registry.meter(
                MetricId.newId("valid_lowercase_name")
                        .label("valid_lowercase_label_name", "label_value"));
    }

    @Test
    public void ensureMetricName_withUppercase_isAllowed() {
        registry.meter(
                MetricId.newId("VALID_UPPERCASE_NAME")
                        .label("VALID_UPPERCASE_LABEL_NAME", "LABEL_VALUE"));
    }

    @Test
    public void ensureMetricNameWith_digits_isAllowed() {
        registry.meter(MetricId.newId("valid_name_v1").label("valid_label_name_v1", "label_value"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureMetricName_withHyphen_throwsIllegalArgumentException() {
        registry.meter(MetricId.newId("invalid-name-with-hyphen"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void ensureMetricLabelName_withHyphen_throwsIllegalArgumentException() {
        registry.meter(
                MetricId.newId("valid_name").label("invalid-label-name-with-hyphen", "test_value"));
    }
}
