package se.tink.libraries.metrics;

import com.google.inject.Inject;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.Optional;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Note that this class must not be created with the same metricRegistry. */
public class HeapDumpGauge {
    private static final MetricId HEAP_DUMPS_METRIC_NAME = MetricId.newId("heap_dumps");
    private static final Logger log = LoggerFactory.getLogger(HeapDumpGauge.class);
    private final MetricRegistry metricRegistry;

    @Inject
    public HeapDumpGauge(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @PostConstruct
    public void initializeHeapDumpGauge() {
        // Extract where heap dumps are persisted.
        File heapDumpPath = new File(extractHeapDumpPath());
        log.info(String.format("Identified heap dump path: %s", heapDumpPath));

        // Register the gauge that keeps track if any of them have been stored.
        metricRegistry.registerSingleton(
                HEAP_DUMPS_METRIC_NAME,
                new DirectoryCountingGauge(
                        heapDumpPath,
                        pathname ->
                                pathname.getName().endsWith(".hprof")
                                        || pathname.getName().endsWith(".hprof.gz")
                                        || pathname.getName().endsWith(".hprof.bz2")));
    }

    private String extractHeapDumpPath() {
        // Fall back to CWD if no JVM arg is given.
        // See http://stackoverflow.com/a/3153440/260805.
        return extractHeapDumpPathFromJVMParams().orElse(System.getProperty("user.dir"));
    }

    private Optional<String> extractHeapDumpPathFromJVMParams() {
        // See http://stackoverflow.com/a/1531999/260805.
        final List<String> inputArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
        for (String arg : inputArgs) {
            // Looking for something "-XX:HeapDumpPath=/tmp/heapdumps" and extracting the value.
            if (arg.startsWith("-XX:HeapDumpPath=")) {
                return Optional.of(arg.substring(arg.indexOf('=') + 1));
            }
        }
        return Optional.empty();
    }
}
