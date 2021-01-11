package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.metrics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.metrics.registry.MetricRegistry;

@Getter
@Slf4j
public class NemIdMetrics {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MetricRegistry metricRegistry;
    private final Map<NemIdMetricLabel, NemIdTimer> timers;

    public NemIdMetrics(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
        this.timers = new HashMap<>();
    }

    public void executeWithTimer(Runnable runnable, NemIdMetricLabel nemIdMetricLabel) {
        NemIdTimer timer = prepareTimerForLabel(nemIdMetricLabel);
        try {
            timer.start();
            runnable.run();
        } finally {
            timer.stop();
        }
    }

    @SneakyThrows
    public <T> T executeWithTimer(Callable<T> callable, NemIdMetricLabel nemIdMetricLabel) {
        NemIdTimer timer = prepareTimerForLabel(nemIdMetricLabel);
        try {
            timer.start();
            return callable.call();
        } finally {
            timer.stop();
        }
    }

    private NemIdTimer prepareTimerForLabel(NemIdMetricLabel label) {
        timers.putIfAbsent(label, new NemIdTimer());
        return timers.get(label);
    }

    public void saveTimeMetrics() {
        timers.forEach(this::saveTimerMetric);
    }

    private void saveTimerMetric(NemIdMetricLabel label, NemIdTimer timer) {
        double totalTime = timer.getTotalTimeInSeconds();
        if (!label.getCustomBuckets().isEmpty()) {
            metricRegistry
                    .histogram(label.getMetricId(), label.getCustomBuckets())
                    .update(totalTime);
        } else {
            // will use default buckets
            metricRegistry.histogram(label.getMetricId()).update(totalTime);
        }
    }

    public String getTimersLog() {
        try {
            ObjectNode objectNode = objectMapper.createObjectNode();

            Stream.of(NemIdMetricLabel.values())
                    .forEach(
                            label -> {
                                if (timers.containsKey(label)) {
                                    objectNode.put(
                                            label.getName(),
                                            timers.get(label).getTotalTimeInSeconds());
                                } else {
                                    objectNode.put(label.getName(), (Double) null);
                                }
                            });

            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);

        } catch (JsonProcessingException e) {
            log.error("Could not prepare timers log", e);
            return "ERROR";
        }
    }
}
