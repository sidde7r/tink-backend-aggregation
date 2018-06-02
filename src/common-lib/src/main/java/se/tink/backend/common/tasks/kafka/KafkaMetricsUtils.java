package se.tink.backend.common.tasks.kafka;

import java.util.Map;
import org.apache.kafka.common.Metric;
import org.apache.kafka.common.MetricName;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.metrics.Gauge;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;

public class KafkaMetricsUtils {

    private static final LogUtils log = new LogUtils(StreamingKafkaConsumer.class);

    public static void registerMetrics(Class<?> klass, MetricRegistry registry,
            Map<MetricName, ? extends Metric> metrics) {

        for (final Map.Entry<MetricName, ? extends org.apache.kafka.common.Metric> metric : metrics.entrySet()) {

            MetricId metricName = getMetricName(klass, metric.getKey());

            registry.registerSingleton(metricName, new Gauge() {
                @Override
                public double getValue() {
                    return metric.getValue().value();
                }
            });

        }
    }

    public static void removeMetrics(Class<?> klass, MetricRegistry registry,
            Map<MetricName, ? extends Metric> metrics) {
        for (final Map.Entry<MetricName, ? extends org.apache.kafka.common.Metric> metric : metrics.entrySet()) {

            MetricId metricName = getMetricName(klass, metric.getKey());

            registry.remove(metricName);
        }
    }

    private static MetricId getMetricName(Class<?> klass, MetricName kafkaMetricName) {
        return MetricId.newId("kafka_imported_metrics")
                .label("group", kafkaMetricName.group())
                .label("metric", kafkaMetricName.name());
    }

}
